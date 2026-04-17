# Contribuyendo

<p align="center">
  <a href="CONTRIBUTING.md">English</a> |
  <a href="CONTRIBUTING.zh-CN.md">简体中文</a> |
  <a href="CONTRIBUTING.de-DE.md">Deutsch</a> |
  <a href="CONTRIBUTING.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="CONTRIBUTING.ko-KR.md">한국어</a>
</p>

## Proceso

1. Envía un issue describiendo los cambios que deseas implementar. Si son solo cambios menores/correcciones de errores, puedes saltar al paso 3.
2. Después de discutir el alcance en el issue, asígnatelo a ti mismo. Debería aparecer en la columna "To do" en el proyecto OpenBot.
3. Haz un fork del proyecto y clónalo localmente:

   `git clone https://github.com/<user_id>/OpenBot.git`

4. Crea una rama:

   `git checkout -b <nombre-de-la-rama>`

   donde `<nombre-de-la-rama>` describe concisamente el alcance del trabajo.

5. Realiza el trabajo, escribe buenos mensajes de commit, empuja tu rama al repositorio bifurcado:

   ```bash
   git add <archivo modificado>
   git commit -m <descripción significativa>
   git push --set-upstream origin <nombre-de-la-rama>
   ```

6. Crea una [pull request](https://github.com/ob-f/OpenBot/pulls) en GitHub y enlaza el issue a ella. Debería aparecer en la columna "In progress" en el proyecto OpenBot.
7. Trabaja en cualquier comentario de revisión de código que puedas recibir y empújalo a tu fork. La pull request se actualiza automáticamente.
8. Tómate una bebida fría de tu elección para recompensarte por hacer del mundo un lugar mejor.

## Directrices

- Usa el mismo estilo y formato que el resto del código.
  - Para el código Java (Android) y Python, consulta [abajo](#Formatting).
  - Para cualquier otro código, solo intenta integrarte.
- Actualiza la documentación asociada con los cambios de código que realizaste.
- Si deseas incluir dependencias de terceros, por favor discútelo primero en el issue.
- Las pull requests deben implementar características individuales con la menor cantidad de cambios posible.
- Asegúrate de no incluir archivos temporales o binarios (los gitignores deberían encargarse de esto en su mayoría).
- Rebase/merge master en tu rama antes de enviar la pull request.
- Si es posible, prueba tu código en Windows, Linux y OSX.

## Formateo

### Java

Usamos un script de gradle para formatear el código java. Asegúrate de estar en el directorio `android`.

Puedes verificar tu código con:

```bash
./gradlew checkStyle
```

Puedes aplicar el estilo a todos los archivos ejecutando:

```bash
./gradlew applyStyle
```

### Python

Usamos [black](https://pypi.org/project/black/) para formatear el código python.

Puedes verificar tu código en el directorio actual con:

```bash
black --check .
```

Puedes aplicar el estilo a todos los archivos en el directorio actual ejecutando:

```bash
black .
```

## Recursos adicionales

Si estás buscando más información sobre cómo contribuir a proyectos de código abierto, aquí tienes dos buenas referencias:

- [How to Contribute to Open Source](http://opensource.guide/how-to-contribute/)
- [The beginner's guide to contributing to a GitHub project](https://akrabat.com/the-beginners-guide-to-contributing-to-a-github-project/)

¡Muchas gracias!
