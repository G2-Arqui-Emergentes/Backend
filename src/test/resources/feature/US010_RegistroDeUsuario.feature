@US010 @registro @visitante
Feature: Registro de usuario
  Como visitante
  Quiero registrarme en la plataforma proporcionando mis datos básicos
  Para crear una cuenta y acceder a las funcionalidades de TaskMaster

  Scenario: Registro exitoso con datos válidos
    Given un visitante en la pantalla de registro
    And proporciona datos válidos (nombre, correo y contraseña)
    When completa el formulario y selecciona "Registrarse"
    Then el sistema crea una nueva cuenta
    And permite el acceso a la plataforma

  Scenario: Registro fallido con correo ya existente
    Given un visitante intenta registrarse con un correo ya existente
    When completa el formulario y selecciona "Registrarse"
    Then el sistema rechaza la acción
    And muestra un mensaje indicando que el correo ya está en uso
