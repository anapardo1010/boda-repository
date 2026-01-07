# Refactorización del Sistema de Invitados - Boda

## 🎯 Objetivo
Refactorizar la lógica de actualización de invitados y gestión de pases adicionales para:
- ✅ Evitar pérdida de datos cuando el admin actualiza configuración
- ✅ Usar soft-delete en lugar de DELETE físico
- ✅ Implementar actualización incremental (PATCH) en lugar de DELETE+INSERT
- ✅ Separar claramente invitados principales de pases adicionales

---

## 🔄 Cambios Implementados

### 1. **Entidad InvitadoPersona** 
**Archivo:** `src/main/java/com/example/demo/entity/InvitadoPersona.java`

**Cambio:** Agregado campo `activo` para soft-delete
```java
@Column(nullable = false, columnDefinition = "boolean default true")
private Boolean activo = true;  // Soft delete - false para ocultar sin eliminar de DB
```

**Impacto:** 
- Ya NO se eliminan registros de la base de datos
- Los registros se marcan como `activo=false` cuando se "eliminan"
- Preserva historial completo de datos

---

### 2. **AdminController - Actualización Incremental**
**Archivo:** `src/main/java/com/example/demo/controller/AdminController.java`

**Cambios:**
1. **Método `updateInvitado()`** - Refactorizado completamente:
   - ✅ Usa comparación de nombres para identificar cambios
   - ✅ Soft-delete con `activo=false` en lugar de removeIf()
   - ✅ Actualiza nombres si cambiaron (correcciones tipográficas)
   - ✅ NO toca pases adicionales del usuario
   - ✅ Preserva confirmaciones existentes

2. **Método `createInvitado()`:**
   - Establece `activo=true` por defecto en nuevas personas

3. **Método `exportarLista()`:**
   - Filtra por `activo=true && confirmado=true`

**Lógica:**
```java
// 1. Obtener principales activos
List<InvitadoPersona> principalesActuales = existingInv.getPersonas().stream()
    .filter(p -> !p.getEsAdicional() && p.getActivo())
    .toList();

// 2. Comparar con nueva lista
for (InvitadoPersona actual : principalesActuales) {
    if (!nuevosMap.containsKey(nombreKey)) {
        // SOFT DELETE
        actual.setActivo(false);
    } else {
        // ACTUALIZAR nombre si cambió
        actual.setNombreCompleto(nueva.getNombreCompleto());
    }
}

// 3. Agregar nuevos sin duplicar
```

---

### 3. **InvitadoController - Soft Delete en Confirmaciones**
**Archivo:** `src/main/java/com/example/demo/controller/InvitadoController.java`

**Cambios en `confirmarAsistencia()`:**

1. **Invitados Principales (esAdicional=false):**
   - Solo actualiza campo `confirmado`
   - NUNCA elimina ni desactiva
   - Filtra por `activo=true`

2. **Pases Adicionales (esAdicional=true):**
   - ✅ Soft-delete: marca `activo=false` en lugar de removeIf()
   - ✅ Preserva registros existentes cuando se actualizan
   - ✅ Reactiva con `activo=true` si se vuelve a usar
   - ✅ Crea nuevos solo si no tienen ID

3. **Cálculo de `pasesConfirmados`:**
   - Filtra por `activo=true && confirmado=true`

**Lógica:**
```java
// Soft delete en adicionales
for (InvitadoPersona actual : adicionalesActuales) {
    if (!idsEnviados.contains(actual.getId())) {
        actual.setActivo(false);  // SOFT DELETE
        actual.setConfirmado(false);
    }
}

// Actualizar o crear
if (pa.getPersonaId() != null) {
    // ACTUALIZAR existente (preserva datos)
    persona.setActivo(true);  // Reactivar
} else {
    // CREAR nuevo
}
```

---

### 4. **Frontend - Filtrado por Estado Activo**

**index.html:**
```javascript
// Filtrar solo personas ACTIVAS
const personasActivas = currentInvitado.personas ? 
    currentInvitado.personas.filter(p => p.activo !== false) : [];

// Separar principales y adicionales
const personasPreLlenadas = personasActivas.filter(p => !p.esAdicional);
const personasAdicionalesExistentes = personasActivas.filter(p => p.esAdicional);
```

**admin.html:**
```javascript
// Solo mostrar personas ACTIVAS y CONFIRMADAS
const personasConfirmadas = inv.personas.filter(p => 
    p.activo !== false && p.confirmado
);

// En edición: solo cargar principales activos
const personasEspecificas = inv.personas.filter(p => 
    !p.esAdicional && p.activo !== false
);
```

---

## ✅ Casos de Prueba (Testing Scenarios)

### **Caso A: Regalo de la Novia - Persistencia de Datos**
**Objetivo:** Verificar que aumentar pases NO borra datos existentes

**Pasos:**
1. Como invitado: Entra y confirma con nombres "Juan" y "Maria"
2. Como admin: Aumenta pases adicionales de 2 a 3
3. Como invitado: Refresca la página

**Resultado Esperado:** ✅
- "Juan" y "Maria" siguen visibles y confirmados
- Aparece un tercer espacio vacío disponible
- NO se perdieron datos

**Resultado de Fallo:** ❌
- Nombres desaparecieron
- Confirmación se reinició

---

### **Caso B: Soft Delete - No Destrucción**
**Objetivo:** Verificar que desmarcar un pase NO elimina el registro de DB

**Pasos:**
1. Como invitado: Desmarca "Maria" y guarda
2. Revisa la base de datos

**Resultado Esperado:** ✅
```sql
SELECT * FROM invitado_personas WHERE nombre_completo = 'Maria';
-- Debe existir el registro con:
-- activo = false
-- confirmado = false
```

**Resultado de Fallo:** ❌
- El registro desapareció completamente de la tabla

---

### **Caso C: Separación de Secciones**
**Objetivo:** Verificar separación visual de principales vs adicionales

**Pasos:**
1. Carga invitación con ambos tipos de invitados

**Resultado Esperado:** ✅
- Sección "Invitados" (principales) separada visualmente
- Sección "Pases Adicionales" en su propio contenedor
- No se mezclan en una lista única

**Resultado de Fallo:** ❌
- Todo aparece mezclado sin distinción

---

### **Caso D: Edición de Nombre Post-Confirmación**
**Objetivo:** Verificar que corregir nombre NO afecta confirmación

**Pasos:**
1. Como admin: Corrige "Jhon" por "John" sin tocar número de pases
2. Verifica estado de confirmación

**Resultado Esperado:** ✅
```java
// El registro se actualiza, NO se reemplaza:
persona.setNombreCompleto("John");  // Solo cambia nombre
// confirmado y activo se mantienen intactos
```

**Resultado de Fallo:** ❌
- Se perdió la confirmación
- Se creó registro duplicado

---

### **Caso E: Admin Aumenta Pases - Datos Preservados**
**Objetivo:** Caso crítico - admin aumenta capacidad sin perder datos

**Estado Inicial:**
```
Familia: García
Pases Totales: 2
Principales: ["Carlos García", "Ana García"]
Adicionales: ["Pedro López"] (confirmado)
```

**Acción Admin:** Aumenta pases de 2 a 4

**Resultado Esperado:** ✅
```
Familia: García
Pases Totales: 4
Principales: ["Carlos García", "Ana García"] (sin cambios)
Adicionales: ["Pedro López" (confirmado)] + 1 espacio vacío nuevo
```

**Consulta SQL para verificar:**
```sql
SELECT nombre_completo, confirmado, activo, es_adicional 
FROM invitado_personas 
WHERE invitado_id = X;

-- Debe mostrar:
-- Carlos García | true  | true | false
-- Ana García    | true  | true | false  
-- Pedro López   | true  | true | true  (PRESERVADO)
```

---

## 🔍 Consultas SQL para Verificación

### Ver todos los registros (incluyendo inactivos):
```sql
SELECT 
    i.nombre_familia,
    ip.nombre_completo,
    ip.confirmado,
    ip.activo,
    ip.es_adicional
FROM invitado_personas ip
JOIN invitados i ON ip.invitado_id = i.id
ORDER BY i.nombre_familia, ip.orden;
```

### Ver solo registros activos:
```sql
SELECT * FROM invitado_personas 
WHERE activo = true;
```

### Ver registros "eliminados" (soft-deleted):
```sql
SELECT * FROM invitado_personas 
WHERE activo = false;
```

### Verificar integridad después de actualización:
```sql
-- Antes de actualización
SELECT COUNT(*) as total_antes FROM invitado_personas WHERE invitado_id = X;

-- Después de actualización
SELECT COUNT(*) as total_despues FROM invitado_personas WHERE invitado_id = X;

-- total_despues >= total_antes (nunca debe disminuir)
```

---

## 🚀 Próximos Pasos

1. **Ejecutar Tests:**
   ```bash
   ./mvnw clean test
   ```

2. **Recompilar:**
   ```bash
   ./mvnw clean compile
   ```

3. **Reiniciar Aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Ejecutar Casos de Prueba:** Seguir los 5 casos documentados arriba

5. **Migración de Base de Datos:**
   - El nuevo campo `activo` se agrega automáticamente
   - Registros existentes tendrán `activo = true` por defecto
   - No requiere migración manual

---

## 📊 Resumen de Beneficios

| Antes | Después |
|-------|---------|
| ❌ DELETE físico | ✅ Soft delete con `activo` |
| ❌ removeIf() destruye datos | ✅ Actualización incremental |
| ❌ Se pierden confirmaciones | ✅ Datos preservados |
| ❌ Sincronización compleja | ✅ Lógica simplificada |
| ❌ Sin historial | ✅ Historial completo en DB |

---

## 🔐 Principios de la Refactorización

1. **Nunca DELETE, siempre SOFT DELETE**
2. **Actualizar, no reemplazar**
3. **Separar responsabilidades (admin vs usuario)**
4. **Filtrar por activo en queries**
5. **Preservar historial completo**

---

Fecha: 6 de enero de 2026
Estado: ✅ COMPLETADO
