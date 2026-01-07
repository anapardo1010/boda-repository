# Guía de Testing - Sistema de Invitaciones Refactorizado

## Escenario Completo de Prueba

### **PASO 1: Admin crea invitación (5 pases, 3 pre-llenados)**

1. Abre panel admin
2. Haz clic en el botón `+` para agregar invitado
3. Completa:
   - Nombre Familia: "García Pérez"
   - Slug: "garcia-perez"
   - Teléfono: +521234567890
   - Pases: **5**
4. En "Nombres de Invitados Específicos", llena SOLO 3 nombres:
   - Campo 1: Juan García
   - Campo 2: Maria García
   - Campo 3: Pedro García
   - Campos 4 y 5: (vacíos, no llenar)
5. Guarda

**Resultado esperado en BD:**
```
- Juan García (esAdicional=false, confirmado=false, activo=true)
- Maria García (esAdicional=false, confirmado=false, activo=true)
- Pedro García (esAdicional=false, confirmado=false, activo=true)
```

---

### **PASO 2: Invitado confirma 3 pre-llenados + agrega 1 adicional**

1. Copia el link de invitación
2. Abre en nueva pestaña/navegador (sin autenticar)
3. El modal muestra:
   - Sección "Invitados": Juan García, Maria García, Pedro García (con toggles)
   - Sección "Pases Adicionales": 2 campos vacíos (5 total - 3 pre-llenados = 2 espacios)

4. Confirma:
   - Enciende toggle para Juan
   - Enciende toggle para Maria
   - Enciende toggle para Pedro
   - En el primer "Pase Adicional": agrega nombre "Luis López"
   - Deja el segundo pase adicional vacío

5. Guarda

**Resultado esperado en BD:**
```
- Juan García (esAdicional=false, confirmado=true, activo=true)
- Maria García (esAdicional=false, confirmado=true, activo=true)
- Pedro García (esAdicional=false, confirmado=true, activo=true)
- Luis López (esAdicional=true, confirmado=true, activo=true) ← NUEVO
```

**Estado en BD:**
- pasesConfirmados = 4 (3 principales + 1 adicional)
- confirmado = true

---

### **PASO 3: Admin AUMENTA pases de 5 a 6**

1. Abre panel admin
2. Busca "García Pérez" y haz clic en editar (icono lápiz)
3. En el modal:
   - Debería ver los 3 nombres pre-llenados: Juan, Maria, Pedro
   - **DEBERÍA VER nueva sección:** "Pases Adicionales Agregados" con Luis López marcado como "(Agregado por invitado)"
   - Los 3 primeros campos de "Nombres Específicos" están llenos
   - Los campos 4 y 5 están vacíos

4. Cambia "Pases" de 5 a **6**
5. Los campos se actualizan automáticamente:
   - Campos 1-3: Juan, Maria, Pedro (SE PRESERVAN)
   - Campos 4-5: Vacíos (se mantienen vacíos, no se reinician)
   - Campo 6: Nuevo campo vacío (pase regalado)

6. Guarda sin cambiar nada más

**Resultado esperado en BD:**
```
- Juan García (esAdicional=false, confirmado=true, activo=true)
- Maria García (esAdicional=false, confirmado=true, activo=true)
- Pedro García (esAdicional=false, confirmado=true, activo=true)
- Luis López (esAdicional=true, confirmado=true, activo=true) ← PRESERVADO
```

**Estado en BD:**
- pasesTotales = 6 (actualizado)
- pasesConfirmados = 4 (SIN CAMBIOS - solo él puede editar)
- confirmado = true (SIN CAMBIOS)
- Luis López (COMPLETAMENTE INTACTO)

---

### **PASO 4: Invitado entra nuevamente para usar el 6to pase**

1. El invitado entra de nuevo con su link
2. El modal muestra:
   - **Sección "Invitados":** Juan, Maria, Pedro (con toggles, estado guardado)
   - **Sección "Pases Adicionales":** Luis López (existente, puede editar nombre o desactivar) + **2 campos vacíos nuevos** (antes había 1, ahora hay 2)
   
3. El invitado puede:
   - Editar el nombre "Luis López" → "Luis López García"
   - Agregar nombre en el primer campo nuevo → "Ana Martín"
   - Dejar el segundo campo vacío
   - O desactivar a Luis (quitar toggle) → Luis se marcaría como inactivo

4. Guarda cambios

**Resultado esperado:**
```
- Juan García (esAdicional=false, confirmado=true)
- Maria García (esAdicional=false, confirmado=true)
- Pedro García (esAdicional=false, confirmado=true)
- Luis López García (esAdicional=true, confirmado=true) ← ACTUALIZADO NOMBRE
- Ana Martín (esAdicional=true, confirmado=true) ← NUEVO
```

**Estado en BD:**
- pasesConfirmados = 5 (actualizado: 3 + 2 adicionales)
- confirmado = true

---

## Validaciones Clave

### ✅ En el Admin:

- [ ] Sección "Nombres de Invitados Específicos" solo muestra los principales (esAdicional=false)
- [ ] Los nombres pre-llenados se preservan cuando cambias el número de pases
- [ ] Nueva sección "Pases Adicionales Agregados" muestra en gris/read-only
- [ ] Los adicionales NO se pueden editar desde admin, solo se ven
- [ ] Al guardar, los adicionales no se pierden

### ✅ En el Invitado:

- [ ] Sección "Invitados" muestra SOLO personas principales (3 en este caso)
- [ ] Sección "Pases Adicionales" muestra SOLO personas adicionales (Luis, + espacios vacíos)
- [ ] Puede editar nombres en "Pases Adicionales"
- [ ] Puede desactivar un pase adicional (lo marca como inactivo)
- [ ] El contador de confirmados es correcto
- [ ] En admin NO aparece "Invitación no encontrada" al final

### ✅ En BD:

- [ ] `esAdicional` es correcto en cada registro
- [ ] `activo=false` para registros "eliminados" (nunca DELETE)
- [ ] `confirmado` refleja estado correcto
- [ ] `pasesTotales` se actualiza sin afectar personas existentes
- [ ] `pasesConfirmados` solo lo controla el invitado

---

## Consultas SQL para Validar

```sql
-- Ver todo para una familia
SELECT 
    nombre_completo,
    confirmado,
    activo,
    es_adicional,
    orden
FROM invitado_personas
WHERE invitado_id = (SELECT id FROM invitados WHERE slug = 'garcia-perez')
ORDER BY orden;

-- Resultado esperado después de PASO 4:
-- Juan García | true | true | false | 1
-- Maria García | true | true | false | 2
-- Pedro García | true | true | false | 3
-- Luis López García | true | true | true | 4
-- Ana Martín | true | true | true | 5

-- Verificar que el pase "extra" (si lo hubiera desactivado) queda:
SELECT * FROM invitado_personas WHERE nombre_completo = 'Luis López' AND invitado_id = X;
-- Si desactivó: activo = false, confirmado = false, es_adicional = true (REGISTRO INTACTO)
```

---

## Problemas Comunes y Soluciones

### Problema: "Luis aparece en Invitados en lugar de Pases Adicionales"
- **Causa:** esAdicional=false en la BD
- **Solución:** Verificar que confirmarAsistencia() en backend establece `persona.setEsAdicional(true)`

### Problema: "Nombres de admin se reinician al aumentar pases"
- **Causa:** updatePersonasFieldsPreservingData() no está leyendo DOM correctamente
- **Solución:** Verificar que obtiene `currentData` del DOM antes de regenerar

### Problema: "Admin puede editar Luis"
- **Causa:** Filter en editInvitado() no está filtrando esAdicional
- **Solución:** Verificar `!p.esAdicional` en el filter

### Problema: "Error de conexión" al guardar
- **Causa:** DTO PersonaAdicional malformado
- **Solución:** Verificar que envía `{personaId, nombre}` correctamente

---

## Estados del Sistema

| Persona | Admin Ve | Invitado Ve | Puede Editar | Puede Eliminar |
|---------|----------|-------------|--------------|----------------|
| Juan (principal, confirmado) | Sí, editable | Sí, toggle | Admin | Admin (soft-delete) |
| Luis (adicional, confirmado) | Sí, read-only | Sí, editable | Invitado | Invitado (soft-delete) |
| Campo vacío (6to pase) | Sí, editable | Sí, editable | Admin/Invitado | - |

