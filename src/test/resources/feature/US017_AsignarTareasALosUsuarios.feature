@US017 @tareas @lider
Feature: Asignar tareas a los usuarios
  Como líder de equipo
  Quiero asignar tareas específicas a los miembros
  Para distribuir la carga de trabajo de forma clara

  Scenario: Asignación exitosa de tarea
    Given el líder selecciona una tarea existente
    And elige un usuario responsable
    When guarda la asignación
    Then el sistema registra la asignación
    And muestra el nombre del responsable en el tablero

  Scenario: Asignación fallida a usuario inexistente
    Given el líder intenta asignar una tarea a un usuario inexistente
    When confirma la acción
    Then el sistema muestra un mensaje de error
    And no guarda la asignación
