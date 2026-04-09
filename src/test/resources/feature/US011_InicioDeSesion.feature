@US011 @login @usuario
Feature: Inicio de sesión
  Como usuario registrado
  Quiero autenticarme con mis credenciales
  Para acceder a mi cuenta y gestionar mis proyectos

  Scenario: Inicio de sesión exitoso
    Given un usuario registrado en la plataforma
    And proporciona credenciales válidas
    When solicita autenticación
    Then el sistema valida los datos
    And concede acceso al panel principal

  Scenario: Inicio de sesión fallido
    Given un usuario intenta autenticarse con credenciales inválidas
    When solicita autenticación
    Then el sistema rechaza la acción
    And muestra un mensaje de error indicando que los datos son incorrectos
