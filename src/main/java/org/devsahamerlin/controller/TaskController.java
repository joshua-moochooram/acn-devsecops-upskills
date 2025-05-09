package org.devsahamerlin.controller;

import lombok.AllArgsConstructor;
import org.devsahamerlin.model.Task;
import org.devsahamerlin.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private static final String REDIRECT = "redirect:/";

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tasks", taskService.findTasks());
        model.addAttribute("newTask", new Task());
        return "index";
    }

    @PostMapping("/tasks")
    public String addTask(@ModelAttribute Task task) {
        taskService.addTask(task);
        return REDIRECT;
    }

    @PostMapping("/tasks/update")
    public String updateTask(@ModelAttribute Task task) {
        taskService.updateTask(task);
        return REDIRECT;
    }

    @GetMapping("/tasks/edit/{id}")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Task task = taskService.findTask(id);
        model.addAttribute("task", task);
        return "update-task";
    }

    @GetMapping("/tasks/{id}")
    public String getTask(@PathVariable String id, Model model) {
        Task task = taskService.findTask(id);
        model.addAttribute("task", task);
        return "task-details";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return REDIRECT;
    }

}
