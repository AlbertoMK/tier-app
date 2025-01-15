## Unir varios commits en uno  
> [!WARNING]  
> ATENCIÓN: se recomienda clonar de 0 la rama previo a los comandos ya que se termina haciendo un push --force y puede sobreescribir cambios que ya se encuentren en el repositorio remoto.

```
git reset --soft <commit hash anterior a los que quieras unir>  
git commit -m <mensaje>  
git push --force  
```
Ejemplo:  
Con el siguiente log:  
hash3 Commit 3  
hash2 Commit 2  
hash1 Commit 1

Queremos unir los commits 2 y 3:
```
git reset --soft hash1
git commit -m "Unión de los commits 2 y 3"  
git push --force
```
Y el resultado de la unión sería la siguiente:  
hash4 Unión de los commits 2 y 3  
hash1 Commit 1
