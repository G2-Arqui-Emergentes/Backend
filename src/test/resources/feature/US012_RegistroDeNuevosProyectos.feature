@US012 @proyectos @lider
Feature: Registro de nuevos proyectos
  Como líder de equipo
  Quiero crear un nuevo proyecto
  Para organizar las tareas y asignar responsables de manera estructurada

  Scenario: Creación exitosa de un proyecto
    Given el líder accede a la opción "Crear proyecto"
    And completa los campos requeridos (nombre, descripción y fechas)
    When confirma la acción
    Then el sistema guarda el nuevo proyecto
    And lo muestra en el tablero del líder

  Scenario: Creación fallida sin nombre de proyecto
    Given el líder intenta crear un proyecto sin nombre
    When hace clic en "Guardar"
    Then el sistema muestra un mensaje de error indicando que el nombre es obligatorio
