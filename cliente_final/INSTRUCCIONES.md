# 🎮 Cómo Jugar Batalla Naval Online

**Versión del Cliente:** 1.0-SNAPSHOT  
**Compatible con:** Java 11+  
**Servidor:** Azure VM (68.211.112.149:8080)  

---

## ✅ Requisitos Previos

### En tu Computadora
- ✅ **Java instalado** (versión 11 o superior)
  - Verificar: Abre CMD/PowerShell y escribe `java -version`
  - Si no está instalado: https://www.java.com/es/download/

### Conexión
- ✅ **Conexión a Internet** (para conectar al servidor remoto)
- ⚠️ Si usas VPN o proxy corporativo, puede ser necesario configurar

---

## 🚀 Cómo Empezar (3 Pasos)

### Opción 1: LA MÁS FÁCIL 👍

1. **Localiza el archivo:** `JUGAR.bat` en esta carpeta
2. **Haz doble clic** en `JUGAR.bat`
3. **¡Listo!** El juego se abrirá automáticamente

### Opción 2: Por Línea de Comandos

1. Abre **PowerShell** o **CMD**
2. Navega a la carpeta donde está `battleship-client.jar`
3. Ejecuta:
   ```
   java -jar battleship-client.jar 68.211.112.149 8080
   ```
4. Se abre la ventana de juego

### Opción 3: Crear Acceso Directo (Recomendado)

1. Haz clic derecho en `JUGAR.bat`
2. Enviar a > Escritorio (crear acceso directo)
3. Ahora puedes hacer doble clic en el escritorio para jugar

---

## 👤 Al Iniciar el Juego

1. **Se abrirá una ventana** pidiendo tu nombre
2. **Escribe tu nombre** de jugador (mínimo 2 caracteres)
   - Ejemplos: "Juan", "María", "Player1", etc.
3. **Haz clic en OK** o presiona Enter
4. **Espera a conectar** - Verás el mensaje:
   ```
   ✓ Conectado al servidor
   ```

---

## 🎯 Cómo Jugar

### Paso 1: COLOCAR BARCOS ⚓

**En TU TABLERO (izquierdo):**

1. **Haz clic** en la primera celda del tablero
2. **Aparecerá un botón** 🔄 para cambiar orientación
3. **Haz clic** para marcar dónde COMIENZA el barco
4. **Haz clic** donde debe TERMINAR el barco
5. **Repite** hasta colocar los 5 barcos:
   - 1 barco de 5 casillas
   - 1 barco de 4 casillas
   - 2 barcos de 3 casillas
   - 1 barco de 2 casillas

**Cuando termines:**
- Haz clic en el botón **✅ ¡LISTO PARA JUGAR!**

### Paso 2: ESPERAR AL OPONENTE ⏳

- Espera a que otro jugador se conecte
- Ambos reciben: "¡Jugador conectado! Coloca tus barcos"

### Paso 3: ATACAR 🎯

**Una vez que ambos presionaron ✅:**

1. En el TABLERO ENEMIGO (derecho): **Haz clic** para atacar
2. Verás el resultado:
   - 💧 **AGUA** = No acertaste (pasa turno)
   - 🔥 **IMPACTO** = Golpeaste un barco (continúa tu turno)
   - 💀 **HUNDIDO** = Barco completamente destruido
3. ¡Continúa hasta ganar!

---

## ⚠️ Problemas Comunes

### "Java no se reconoce como comando"
- Instala Java desde: https://www.java.com/es/download/
- Reinicia tu terminal

### "Error: Unable to access jarfile"
- Verifica que `battleship-client.jar` esté en la misma carpeta
- Abre PowerShell en esa carpeta

### "Connection failed"
- ¿Tienes Internet?
- ¿El servidor está corriendo?
- ¿Tu firewall bloquea Java?

### "No puedo colocar un barco"
- ¿Sale de los límites? (10x10)
- ¿Se superpone con otro?
- ¿Cambiste de orientación?

---

## 📊 Interfaz del Juego

- **TU TABLERO** (izquierda) = Ves tus barcos
- **TABLERO ENEMIGO** (derecha) = Atacas aquí
- **✅ ¡LISTO PARA JUGAR!** = Confirma tus barcos
- **Mensajes** = Ven los eventos de la partida

---

## 🎓 Conceptos Educativos

Este juego enseña:
- ✅ WebSocket (comunicación en tiempo real)
- ✅ JSON (intercambio de datos)
- ✅ Arquitectura Cliente-Servidor
- ✅ Interfaz Gráfica (Swing)
- ✅ Programación Concurrente

---

## 📱 Requisitos Mínimos

| Requisito | Especificación |
|-----------|--------|
| **Java** | 11 o superior |
| **RAM** | 256 MB mínimo |
| **Red** | Conexión a Internet |
| **Pantalla** | 1024x768 mínimo |

---

## 🎉 ¡A Jugar!

Cuando veas la ventana del juego con dos tableros lado a lado:

**Eres uno de dos jugadores compitiendo en tiempo real**

- Coloca tus barcos
- Espera a tu oponente
- ¡Ataca y defiéndete!
- ¡Gana hundiendo sus barcos!

---

**Versión:** 1.0-SNAPSHOT  
**Última actualización:** 27/11/2025  
**Servidor:** 68.211.112.149:8080
