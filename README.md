## PS6 - Networked Sketch Editor

A client/server Swing drawing program. The server maintains a shared sketch state; clients send requests to add/delete/move/recolor shapes.

### Run
1. Start the server (Terminal 1):
   - `src/SketchServer.java` (runs on port `4242`)
2. Start the client (Terminal 2):
   - `src/Editor.java` (connects to `localhost:4242` by default)

### Notes
- If you run the client and server on different machines, edit `serverIP` in `Editor.java`.

