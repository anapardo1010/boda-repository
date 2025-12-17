# 💒 Invitaciones de Boda - Aplicación Web

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
