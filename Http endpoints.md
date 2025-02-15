## Conecta con el servidor a través de los siguientes endpoints.
> [!NOTE] 
> Por cada uno de ellos, se indica el queryString (parámetros en la propia URL), método HTTP y contenido del body.

### 1. Endpoints relacionados con Usuarios
| Descripción  | Método HTTP | Endpoint | queryString | Atributos del body |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| Devuelve un único usuario | GET | /user | username | N/A |
| Devuelve todos los usuarios | GET | /user | N/A | N/A |
| Crea un nuevo usuario | POST | /user | N/A | Atributos de user. Obligatorios 'username', 'password', 'dateOfBirth' |
| Genera un token para hacer login | POST | /user/login | N/A | 'username', 'password' |
| Crea una solicitud de amistad | POST | /user/friend | N/A | 'session_token', 'requested' (es el username del usuario al que le haces la solicitud) |
| Acepta una solicitud de amistad | POST | /user/friend/accept | N/A | 'session_token', 'requester' (es el username del usuario que mandó la solicitud) |
| Rechaza una solicitud de amistad | POST | /user/friend/reject | N/A | 'session_token', 'requested' (es el username del usuario que mandó la solicitud) |
| Elimina una solicitud de amistad | DELETE | /user/friend | N/A | 'session_token', 'requested' (es el username del usuario del cual quieres eliminar la solicitud) |
