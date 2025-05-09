package org.devsahamerlin.controller;

import org.devsahamerlin.enums.Status;
import org.devsahamerlin.model.Task;
import org.devsahamerlin.repository.TaskRepository;
import org.devsahamerlin.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void testHomeEndpoint() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(
                new Task("1", "Task 1", "Description 1", Status.NOT_COMPLETED, LocalDate.of(2025, 12,28)),
                new Task("2", "Task 2", "Description 2", Status.COMPLETED, LocalDate.of(2025, 12,28))
        );
        when(taskService.findTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("tasks", tasks))
                .andExpect(model().attributeExists("newTask"));

        verify(taskService).findTasks();
    }

    @Test
    void testAddTaskEndpoint() throws Exception {
        // Given
        Task task = new Task(null, "New Task", "New Description", Status.NOT_COMPLETED, LocalDate.of(2025, 12,28));
        when(taskService.addTask(any(Task.class))).thenReturn(task);

        // When & Then
        mockMvc.perform(post("/tasks")
                        .param("title", "New Task")
                        .param("description", "New Description")
                        .param("completed", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(taskService).addTask(any(Task.class));
    }

    @Test
    void testUpdateTaskEndpoint() throws Exception {
        // Given
        Task task = new Task("1", "Updated Task", "Updated Description", Status.COMPLETED, LocalDate.of(2025, 12,28));
        when(taskService.updateTask(any(Task.class))).thenReturn(task);

        // When & Then
        mockMvc.perform(post("/tasks/update")
                        .param("ID", "1")
                        .param("title", "Updated Task")
                        .param("description", "Updated Description")
                        .param("completed", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(taskService).updateTask(any(Task.class));
    }

    @Test
    void testShowUpdateFormEndpoint() throws Exception {
        // Given
        Task task = new Task("1", "Task 1", "Description 1", Status.NOT_COMPLETED, LocalDate.of(2025, 12,28));
        when(taskService.findTask(anyString())).thenReturn(task);

        // When & Then
        mockMvc.perform(get("/tasks/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-task"))
                .andExpect(model().attribute("task", task));

        verify(taskService).findTask("1");
    }

    @Test
    void testGetTaskEndpoint() throws Exception {
        // Given
        Task task = new Task("1", "Task 1", "Description 1", Status.NOT_COMPLETED, LocalDate.of(2025, 12,28));
        when(taskService.findTask(anyString())).thenReturn(task);

        // When & Then
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("task-details"))
                .andExpect(model().attribute("task", task));

        verify(taskService).findTask("1");
    }

    @Test
    void testDeleteTaskEndpoint() throws Exception {
        // Given
        when(taskService.deleteTask(anyString())).thenReturn("Task successfully deleted");

        // When & Then
        mockMvc.perform(get("/tasks/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(taskService).deleteTask("1");
    }
}