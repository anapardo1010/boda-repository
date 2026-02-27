# 💒 Invitaciones de Boda - Aplicación Web

## 🧩 ¿Cómo funciona el proyecto?

Esta aplicación web permite gestionar las invitaciones de boda de manera digital, facilitando la confirmación de asistencia y la interacción con los invitados. El flujo general es:

1. **Acceso personalizado:** Cada familia recibe un enlace único con un identificador (`slug`). Al acceder, se muestra la invitación personalizada y el número de pases disponibles.
2. **Confirmación de asistencia:** Los invitados pueden confirmar cuántos pases usarán y dejar un mensaje especial para los anfitriones.
3. **Visualización de datos:** Los datos de confirmación se almacenan y pueden ser consultados por los organizadores desde el backend.
4. **Cuenta regresiva y ubicación:** La página muestra una cuenta regresiva al evento y un mapa con la ubicación.

### Flujo de usuario

1. El invitado accede a la URL con su `slug` (por ejemplo, `http://localhost:8080/?id=familia-perez`).
2. La aplicación carga los datos de la familia y muestra la invitación personalizada.
3. El invitado confirma su asistencia y envía el formulario.
4. El backend registra la confirmación y muestra un mensaje de agradecimiento.

### Flujo técnico

- El frontend está en `src/main/resources/static/index.html` y utiliza HTML5, Tailwind CSS y JavaScript vanilla.
- El backend está construido con Java 17 y Spring Boot, expone endpoints REST para consultar y confirmar invitados.
- La base de datos H2 almacena los datos temporalmente en memoria.

Puedes probar el flujo usando los slugs de ejemplo y los endpoints descritos abajo.

Una aplicación web elegante para gestionar invitaciones de bodas con confirmación en línea.

## 🚀 Características

- ✨ Diseño elegante y responsivo
- 🎯 Confirmación de asistencia personalizada
- ⏰ Cuenta regresiva en vivo
- 📍 Integración con Google Maps
- 💬 Mensajes especiales de invitados
- 🔐 Acceso por código único (slug)

## 🛠️ Tecnologías

- **Backend**: Java 17, Spring Boot, Spring Data JPA
- **Base de Datos**: H2 (en memoria)
- **Frontend**: HTML5, Tailwind CSS, JavaScript vanilla

## 📖 Cómo Usar

### Desarrollo Local

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/demo-0.0.1-SNAPSHOT.jar

# Acceder
http://localhost:8080/?id=familia-perez
```

### URLs de Prueba

- http://localhost:8080/?id=familia-perez
- http://localhost:8080/?id=familia-garcia
- http://localhost:8080/?id=familia-martinez

## 📁 Estructura

```
src/main/java/com/example/demo/
├── entity/       # Modelos de datos
├── repository/   # Acceso a datos
├── controller/   # Endpoints REST
├── dto/          # Data Transfer Objects
└── component/    # Componentes de aplicación

src/main/resources/
├── application.properties  # Configuración
└── static/index.html       # Frontend
```

## 🔗 API Endpoints

### GET /api/invitados/{slug}
Obtiene datos de una familia por slug.

**Ejemplo:**
```bash
curl http://localhost:8080/api/invitados/familia-perez
```

### POST /api/invitados/confirmar
Confirma la asistencia de una familia.

**Body:**
```json
{
  "slug": "familia-perez",
  "pasesConfirmados": 3,
  "mensaje": "¡Nos vemos allá!"
}
```

## 📝 Datos de Prueba

| Slug | Familia | Pases |
|------|---------|-------|
| familia-perez | Familia Pérez | 4 |
| familia-garcia | Familia García | 3 |
| familia-martinez | Familia Martínez | 5 |

## 👨‍💻 Desarrollo

Para modificar invitados, edita `DataLoader.java`:

```java
@Override
public void run(String... args) throws Exception {
    invitadoRepository.save(new Invitado("slug", "Nombre", pases));
}
```

## 📄 Licencia

MIT
