package org.devsahamerlin.steps.task;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.devsahamerlin.enums.Status;
import org.devsahamerlin.model.Task;
import org.devsahamerlin.repository.TaskRepository;
import org.devsahamerlin.service.TaskService;
import org.devsahamerlin.service.impl.TaskServiceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceSteps {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private Task task;
    private Task foundTask;
    private String resultMessage;
    private List<Task> taskList;
    private RuntimeException thrownException;

    @Before
    public void setup() {
        TaskServiceImpl serviceImpl = new TaskServiceImpl(taskRepository);
        ReflectionTestUtils.setField(serviceImpl, "taskRepository", taskRepository);

        taskService = serviceImpl;
        task = null;
        resultMessage = null;
        taskList = null;
        thrownException = null;
    }

    @Given("the task service is initialized")
    public void theTaskServiceIsInitialized() {
        assertNotNull("Task service should be initialized", taskService);
    }

    @Given("the task repository is empty")
    public void theTaskRepositoryIsEmpty() {
        when(taskRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @When("I create a new task with title {string} and description {string}")
    public void iCreateANewTaskWithTitleAndDescription(String title, String description) {
        task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(Status.COMPLETED);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            if (savedTask.getId() == null) {
                savedTask.setId(UUID.randomUUID().toString());
            }
            return savedTask;
        });

        task = taskService.addTask(task);
    }

    @Then("the task should be saved in the repository")
    public void theTaskShouldBeSavedInTheRepository() {
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Then("the task should have a valid UUID")
    public void theTaskShouldHaveAValidUUID() {
        assertNotNull("Task ID should not be null", task.getId());
        try {
            UUID.fromString(task.getId());
        } catch (IllegalArgumentException e) {
            fail("Task ID is not a valid UUID");
        }
    }


    @Given("there is an existing task with title {string} in the repository")
    public void thereIsAnExistingTaskWithTitleInTheRepository(String title) {
        task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setTitle(title);
        task.setDescription("Initial description");

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
    }

    @When("I delete the task by its ID")
    public void iDeleteTheTaskByItsID() {
        resultMessage = taskService.deleteTask(task.getId());
    }

    @Then("the task should be removed from the repository")
    public void theTaskShouldBeRemovedFromTheRepository() {
        verify(taskRepository, times(1)).deleteById(task.getId());
    }

    @Then("I should receive a message {string}")
    public void iShouldReceiveAMessage(String expectedMessage) {
        assertEquals("The return message should match", expectedMessage, resultMessage);
    }

    @When("I attempt to delete a task with an invalid ID {string}")
    public void iAttemptToDeleteATaskWithAnInvalidID(String invalidId) {
        when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());
        resultMessage = taskService.deleteTask(invalidId);
    }

    @Then("the task should not be removed from the repository")
    public void theTaskShouldNotBeRemovedFromTheRepository() {
        verify(taskRepository, never()).deleteById(any(String.class));
    }

    @When("I update the task title to {string} and description to {string}")
    public void iUpdateTheTaskTitleToAndDescriptionTo(String newTitle, String newDescription) {
        task.setTitle(newTitle);
        task.setDescription(newDescription);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task updatedTask = taskService.updateTask(task);
        task = updatedTask;
    }

    @Then("the task should be updated in the repository")
    public void theTaskShouldBeUpdatedInTheRepository() {
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals("ID should remain the same", task.getId(), capturedTask.getId());
        assertEquals("Title should be updated", task.getTitle(), capturedTask.getTitle());
        assertEquals("Description should be updated", task.getDescription(), capturedTask.getDescription());
    }

    @Then("the task details should be changed")
    public void theTaskDetailsShouldBeChanged() {
        assertEquals("Title should be updated", "Finish project", task.getTitle());
        assertEquals("Description should be updated", "Complete the task management project ASAP", task.getDescription());
    }

    @When("I attempt to update a task with an invalid ID {string}")
    public void iAttemptToUpdateATaskWithAnInvalidID(String invalidId) {
        Task invalidTask = new Task();
        invalidTask.setId(invalidId);
        invalidTask.setTitle("Invalid Task");

        when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());

        try {
            taskService.updateTask(invalidTask);
        } catch (RuntimeException e) {
            thrownException = e;
        }
    }

    @Then("I should receive a runtime exception with message {string}")
    public void iShouldReceiveARuntimeExceptionWithMessage(String exceptionMessage) {
        assertNotNull("An exception should have been thrown", thrownException);
        assertEquals("Exception message should match", exceptionMessage, thrownException.getMessage());
    }

    @Given("there are multiple tasks in the repository")
    public void thereAreMultipleTasksInTheRepository() {
        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task();
        task1.setId(UUID.randomUUID().toString());
        task1.setTitle("Task 1");
        tasks.add(task1);

        Task task2 = new Task();
        task2.setId(UUID.randomUUID().toString());
        task2.setTitle("Task 2");
        tasks.add(task2);

        when(taskRepository.findAll()).thenReturn(tasks);
    }

    @When("I request to find all tasks")
    public void iRequestToFindAllTasks() {
        taskList = taskService.findTasks();
    }

    @Then("I should receive a list of all tasks")
    public void iShouldReceiveAListOfAllTasks() {
        assertNotNull("Task list should not be null", taskList);
        assertEquals("Task list should have 2 items", 2, taskList.size());
        verify(taskRepository, times(1)).findAll();
    }

    @When("I find the task by its ID")
    public void iFindTheTaskByItsID() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        foundTask = taskService.findTask(task.getId());
    }

    @Then("the task should be returned")
    public void theTaskShouldBeReturned() {
        assertNotNull("Found task should not be null", foundTask);
        assertEquals("Found task ID should match", task.getId(), foundTask.getId());
        assertEquals("Found task title should match", task.getTitle(), foundTask.getTitle());
        assertEquals("Found task description should match", task.getDescription(), foundTask.getDescription());

        verify(taskRepository, times(1)).findById(task.getId());
    }

    @When("I attempt to find a task with an invalid ID {string}")
    public void iAttemptToFindATaskWithAnInvalidID(String invalidId) {
        when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());
        foundTask = taskService.findTask(invalidId);
    }

    @Then("the task should not be found")
    public void theTaskShouldNotBeFound() {
        assertNull("Task should not be found", foundTask);

        verify(taskRepository, times(1)).findById(anyString());
    }

}