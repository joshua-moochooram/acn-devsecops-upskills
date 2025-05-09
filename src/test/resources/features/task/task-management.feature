Feature: Task Management Service
  As a user of the task management application
  I want to create, update, delete, and retrieve tasks
  So that I can manage my tasks effectively

  Background:
    Given the task service is initialized
    And the task repository is empty

  Scenario: Creating a new task
    When I create a new task with title "Complete project" and description "Finish the task management project"
    Then the task should be saved in the repository
    And the task should have a valid UUID

  Scenario: Deleting an existing task
    Given there is an existing task with title "Complete project" in the repository
    When I delete the task by its ID
    Then the task should be removed from the repository
    And I should receive a message "Task successfully deleted"

  Scenario: Deleting a non-existing task
    When I attempt to delete a task with an invalid ID "invalid-id"
    Then the task should not be removed from the repository
    And I should receive a message "Tasks with id invalid-id don't exist"

  Scenario: Updating an existing task
    Given there is an existing task with title "Complete project" in the repository
    When I update the task title to "Finish project" and description to "Complete the task management project ASAP"
    Then the task should be updated in the repository
    And the task details should be changed

  Scenario: Updating a non-existing task
    When I attempt to update a task with an invalid ID "invalid-id"
    Then I should receive a runtime exception with message "Tasks id invalid-id don't exist"

  Scenario: Retrieving all tasks
    Given there are multiple tasks in the repository
    When I request to find all tasks
    Then I should receive a list of all tasks

  Scenario: Finding an existing task by ID
    Given there is an existing task with title "Complete project" in the repository
    When I find the task by its ID
    Then the task should be returned

  Scenario: Finding a non-existing task by ID
    When I attempt to find a task with an invalid ID "invalid-id"
    Then the task should not be found