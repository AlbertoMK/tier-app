# Beta-App

## Cómo usar
1. Ejecuta el [script sql](exercises_gym_exercises.sql) en MySql Workbench
2. Modifica el contenido del [fichero de variables de entorno](src/main/resources/application.properties) susitutyendo la Url, usuario y contraseña de la base de datos
3. Ejecuta el servidor, ejecutando la clase [App](src/main/java/server/App.java)
4. Ejecuta el [archivo html inicial](src/main/resources/templates/login.html)
5. Debería estar todo funcionando y tener comunicación entre el sitio web y el servidor levantado

## Segundas responsabilidades
> [!NOTE]
> - **Alonso:** Product Manager  
> - **Unai:** Architecture Manager  
> - **Nico:** User Experience (UX)  
> - **Alberto:** Beta-Tester y teacher


## Normas
> [!IMPORTANT]
>- **[ 1º ]**  Hacer junto con los commits, los testeos de la funcionalidad correspondiente.
>- **[ 2º ]**  No hacer ningún commit sin que ser aprovado por nadie (pull requests).
>- **[ 3º ]**  Nivel de documentación: Clases importantes (modelo, negocio...), sin un nivel de detalle excesivo (sin métodos, atributos ni clases auxiliares).
>- **[ 4º ]**  Para cambios grandes, hacer un documento explicatorio de éste para introducir a otras personas.
>- **[ 5º ]**  Repartición equitativa y específica de trabajo con feedback y retrospectivas.
>- **[ 6º ]**  Reuniones todos los viernes

## Brainstorm

- Aplicación que se puedan poner objetivos individuales a cumplir y los innovador serían los eventos o objetivos grupales  
- Tener una parte competitiva con más gente a nivel colectivo  
- Métricas y logros, estadísticas y mucha información  
- Simplicidad y comodez para el usuario  

- Parte competitiva:  
Retos y logros individuales, autoprogreso.  
Retos colectivos de equipo, entre varios amigos cumplir y superar retos en equipo.  
Retos colectivos contra equipos, entre varios amigos competir entre grupos.  

- Objetivos: A nivel individual o grupal  
Ej. Mi objetivo es a final de este mes hacer 80kg pressbanca y 50km corridos en total.  
Ej. Para esta semana pongo como objetivo grupal hacer un volumen de entrenamiento de 'X' kilos.  

- Misiones: Un objetivo pero generado automáticamente dentro de los campos que se practiquen.  
Ej. Para esta semana hay que hacer 45km corridos entre los participantes del grupo.  

## Objetivos 1ᵃ iteración

- Crear servidor funcional
- Posibilidad de crear cuentas y loguearse
- Crear base de datos con todos los ejercicios de gimnasio
- Acabar pudiendo:
  1. Crearse un usuario
  2. Crear rutinas persistentes
  3. Crear entrenamientos añadiendo ejercicios y series

