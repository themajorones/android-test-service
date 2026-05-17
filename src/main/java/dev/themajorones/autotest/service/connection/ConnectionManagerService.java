package dev.themajorones.autotest.service.connection;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.themajorones.autotest.dto.connection.DockerConnectionRequest;
import dev.themajorones.autotest.dto.connection.OllamaConnectionRequest;
import dev.themajorones.autotest.repository.AndroidVMRepository;
import dev.themajorones.autotest.repository.DockerRepository;
import dev.themajorones.autotest.repository.OllamaRepository;
import dev.themajorones.autotest.repository.TaskLogRepository;
import dev.themajorones.models.client.DockerClient;
import dev.themajorones.models.client.OllamaClient;
import dev.themajorones.models.constants.ConnectionStatusConstant;
import dev.themajorones.models.constants.RabbitMqConstant;
import dev.themajorones.models.constants.TaskLogConstant;
import dev.themajorones.models.dto.AndroidVMDetail;
import dev.themajorones.models.dto.CreateAndroidVMRequest;
import dev.themajorones.models.dto.DockerCapability;
import dev.themajorones.models.dto.OllamaModelSummary;
import dev.themajorones.models.dto.TaskCommandEnvelope;
import dev.themajorones.models.entity.AndroidVM;
import dev.themajorones.models.entity.Docker;
import dev.themajorones.models.entity.Ollama;
import dev.themajorones.models.entity.TaskLog;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class ConnectionManagerService {

    private static final Duration PORT_CHECK_TIMEOUT = Duration.ofSeconds(2);

    private final OllamaRepository ollamaRepository;
    private final DockerRepository dockerRepository;
    private final AndroidVMRepository androidVMRepository;
    private final TaskLogRepository taskLogRepository;
    private final OllamaClient ollamaClient;
    private final DockerClient dockerClient;
    private final RabbitOperations rabbitOperations;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<Ollama> listOllama() {
        return ollamaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ollama getOllama(Integer id) {
        return ollamaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ollama connection not found"));
    }

    @Transactional
    public Ollama createOllama(OllamaConnectionRequest request) {
        Ollama ollama = new Ollama();
        applyOllamaRequest(ollama, request);
        return ollamaRepository.save(ollama);
    }

    @Transactional
    public Ollama updateOllama(Integer id, OllamaConnectionRequest request) {
        Ollama ollama = getOllama(id);
        applyOllamaRequest(ollama, request);
        return ollamaRepository.save(ollama);
    }

    @Transactional
    public void deleteOllama(Integer id) {
        ollamaRepository.delete(getOllama(id));
    }

    @Transactional(readOnly = true)
    public List<OllamaModelSummary> listOllamaModels(Integer id) {
        return ollamaClient.listModels(getOllama(id).getBaseUrl());
    }

    public List<OllamaModelSummary> listOllamaModels(String baseUrl) {
        return ollamaClient.listModels(ollamaClient.normalizeBaseUrl(requireText(baseUrl, "Ollama base URL")));
    }

    @Transactional(readOnly = true)
    public List<Docker> listDocker() {
        return dockerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Docker getDocker(Integer id) {
        return dockerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Docker connection not found"));
    }

    @Transactional
    public Docker createDocker(DockerConnectionRequest request) {
        Docker docker = new Docker();
        applyDockerRequest(docker, request);
        return dockerRepository.save(docker);
    }

    @Transactional
    public Docker updateDocker(Integer id, DockerConnectionRequest request) {
        Docker docker = getDocker(id);
        applyDockerRequest(docker, request);
        return dockerRepository.save(docker);
    }

    @Transactional
    public void deleteDocker(Integer id) {
        dockerRepository.delete(getDocker(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listAndroidVMs() {
        return androidVMRepository.findAllByOrderByIdDesc().stream().map(this::androidVmMap).toList();
    }

    @Transactional(readOnly = true)
    public AndroidVMDetail getAndroidVM(Integer id) {
        AndroidVM vm = getAndroidVmEntity(id);
        String inspectJson = null;
        if (hasText(vm.getContainerId())) {
            inspectJson = dockerClient.inspectContainerJson(vm.getDocker().getBaseUrl(), vm.getContainerId());
        }
        return new AndroidVMDetail().setAndroidVM(vm).setDockerInspectJson(inspectJson);
    }

    @Transactional
    public Map<String, Object> createAndroidVM(CreateAndroidVMRequest request) {
        Docker docker = getDocker(requireId(request.getDockerId(), "Docker connection id"));
        CreateAndroidVMRequest normalized = normalizeAndroidRequest(request);
        AndroidVM vm = androidVMRepository.save(new AndroidVM()
            .setDocker(docker)
            .setName(requireText(normalized.getName(), "Android VM name"))
            .setImage(normalized.getImage())
            .setAccelerationMode(normalized.getAccelerationMode())
            .setStatus(ConnectionStatusConstant.QUEUED));

        TaskLog taskLog = taskLogRepository.save(new TaskLog()
            .setType(TaskLogConstant.Type.CREATE_ANDROID_VM)
            .setStatus(TaskLogConstant.Status.PENDING)
            .setContent(androidVmTaskContent(vm, normalized)));

        TaskCommandEnvelope envelope = new TaskCommandEnvelope()
            .setTaskLogId(taskLog.getId())
            .setType(TaskLogConstant.Type.CREATE_ANDROID_VM)
            .setContent(taskLog.getContent());

        rabbitOperations.convertAndSend(
            RabbitMqConstant.DIRECT_EXCHANGE,
            RabbitMqConstant.Queue.ConnectionManager.ROUTING_KEY,
            writeJson(envelope)
        );
        taskLog.setStatus(TaskLogConstant.Status.QUEUED);
        taskLogRepository.save(taskLog);

        return Map.of("androidVMId", vm.getId(), "taskLogId", taskLog.getId());
    }

    @Transactional
    public AndroidVM stopAndroidVM(Integer id) {
        AndroidVM vm = getAndroidVmEntity(id);
        if (hasText(vm.getContainerId())) {
            dockerClient.stopContainer(vm.getDocker().getBaseUrl(), vm.getContainerId());
        }
        vm.setStatus(ConnectionStatusConstant.STOPPED);
        return androidVMRepository.save(vm);
    }

    @Transactional
    public void deleteAndroidVM(Integer id) {
        AndroidVM vm = getAndroidVmEntity(id);
        if (hasText(vm.getContainerId())) {
            dockerClient.removeContainer(vm.getDocker().getBaseUrl(), vm.getContainerId());
        }
        androidVMRepository.delete(vm);
    }

    @Transactional(readOnly = true)
    public List<TaskLog> listTaskLogs() {
        return taskLogRepository.findTop100ByOrderByIdDesc();
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 5_000)
    @Transactional
    public void refreshConnectionHealth() {
        for (Ollama ollama : ollamaRepository.findAllByEnabledTrue()) {
            try {
                ollamaClient.isHealthy(ollama.getBaseUrl());
                ollama.setStatus(ConnectionStatusConstant.HEALTHY);
            } catch (Exception ex) {
                ollama.setStatus(ConnectionStatusConstant.UNHEALTHY);
            }
        }
        for (Docker docker : dockerRepository.findAllByEnabledTrue()) {
            try {
                applyDockerCapability(docker, dockerClient.capabilities(docker.getBaseUrl()));
                docker.setStatus(ConnectionStatusConstant.HEALTHY);
            } catch (Exception ex) {
                docker.setStatus(ConnectionStatusConstant.UNHEALTHY);
            }
        }
        for (AndroidVM vm : androidVMRepository.findAll()) {
            refreshAndroidVmStatus(vm);
        }
    }

    private void applyOllamaRequest(Ollama ollama, OllamaConnectionRequest request) {
        String baseUrl = ollamaClient.normalizeBaseUrl(requireText(request.getBaseUrl(), "Ollama base URL"));
        List<OllamaModelSummary> models = ollamaClient.listModels(baseUrl);
        String selectedModel = requireText(request.getModel(), "Ollama model");
        boolean modelExists = models.stream().anyMatch(model ->
            selectedModel.equals(model.getName()) || selectedModel.equals(model.getModel()));
        if (!modelExists) {
            throw new IllegalArgumentException("Ollama model is not available on the selected server");
        }
        ollama
            .setName(requireText(request.getName(), "Ollama name"))
            .setBaseUrl(baseUrl)
            .setEnabled(request.getEnabled() == null || request.getEnabled())
            .setModel(selectedModel)
            .setStatus(ConnectionStatusConstant.HEALTHY);
    }

    private void applyDockerRequest(Docker docker, DockerConnectionRequest request) {
        String baseUrl = dockerClient.normalizeBaseUrl(requireText(request.getBaseUrl(), "Docker base URL"));
        DockerCapability capability = dockerClient.capabilities(baseUrl);
        docker
            .setName(requireText(request.getName(), "Docker name"))
            .setBaseUrl(baseUrl)
            .setEnabled(request.getEnabled() == null || request.getEnabled())
            .setStatus(ConnectionStatusConstant.HEALTHY);
        applyDockerCapability(docker, capability);
    }

    private void applyDockerCapability(Docker docker, DockerCapability capability) {
        docker
            .setApiVersion(capability.getApiVersion())
            .setOs(capability.getOs())
            .setArch(capability.getArch())
            .setNvidiaRuntimeAvailable(capability.isNvidiaRuntimeAvailable())
            .setGpuDevicesJson(writeJson(capability.getGpuDevices()));
    }

    private void refreshAndroidVmStatus(AndroidVM vm) {
        if (!hasText(vm.getContainerId()) || ConnectionStatusConstant.DELETED.equals(vm.getStatus())) {
            return;
        }
        try {
            boolean running = dockerClient.isContainerRunning(vm.getDocker().getBaseUrl(), vm.getContainerId());
            if (!running) {
                vm.setStatus(ConnectionStatusConstant.STOPPED);
                return;
            }
            if (ConnectionStatusConstant.READY.equals(vm.getStatus())) {
                return;
            }
            boolean portOpen = dockerClient.isTcpPortReachable(vm.getAdbHost(), vm.getAdbPort(), PORT_CHECK_TIMEOUT);
            vm.setStatus(portOpen ? ConnectionStatusConstant.RUNNING : ConnectionStatusConstant.UNHEALTHY);
        } catch (Exception ex) {
            vm.setStatus(ConnectionStatusConstant.UNHEALTHY);
        }
    }

    private AndroidVM getAndroidVmEntity(Integer id) {
        return androidVMRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Android VM not found"));
    }

    private CreateAndroidVMRequest normalizeAndroidRequest(CreateAndroidVMRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Android VM request is required");
        }
        if (!hasText(request.getImage())) {
            request.setImage(CreateAndroidVMRequest.DEFAULT_IMAGE);
        }
        if (!hasText(request.getAccelerationMode())) {
            request.setAccelerationMode(CreateAndroidVMRequest.DEFAULT_ACCELERATION_MODE);
        }
        request.setAccelerationMode(request.getAccelerationMode().trim().toUpperCase());
        return request;
    }

    private String androidVmTaskContent(AndroidVM vm, CreateAndroidVMRequest request) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("androidVMId", vm.getId());
        root.put("dockerId", vm.getDocker().getId());
        root.put("name", request.getName());
        root.put("image", request.getImage());
        root.put("accelerationMode", request.getAccelerationMode());
        if (request.getWidth() != null) {
            root.put("width", request.getWidth());
        }
        if (request.getHeight() != null) {
            root.put("height", request.getHeight());
        }
        if (request.getDpi() != null) {
            root.put("dpi", request.getDpi());
        }
        return root.toString();
    }

    private Map<String, Object> androidVmMap(AndroidVM vm) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", vm.getId());
        values.put("dockerId", vm.getDocker().getId());
        values.put("dockerName", vm.getDocker().getName());
        values.put("name", vm.getName());
        values.put("image", vm.getImage());
        values.put("containerId", vm.getContainerId());
        values.put("containerName", vm.getContainerName());
        values.put("adbHost", vm.getAdbHost());
        values.put("adbPort", vm.getAdbPort());
        values.put("accelerationMode", vm.getAccelerationMode());
        values.put("status", vm.getStatus());
        return values;
    }

    private Integer requireId(Integer value, String description) {
        if (value == null) {
            throw new IllegalArgumentException(description + " is required");
        }
        return value;
    }

    private String requireText(String value, String description) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(description + " is required");
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize JSON", ex);
        }
    }
}
