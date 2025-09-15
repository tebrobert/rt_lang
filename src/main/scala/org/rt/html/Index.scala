package org.rt.html

object Index {
  val page = """
<html>
  <head>
    <title>RT Lang</title>
    <style>
      html, body, div, textarea { width: 100%; height: 100%; }
      textarea { resize: none; font-size: 20pt; font-family: 'Courier New', Courier, monospace; }
      button { padding:10px; width:45%; background-color:darkblue; color:lightgray}
    </style>
  </head>
  <body style="background-color:gray;" onload="clear()">
    <div class="container" style="display:flex; height:98%;">
      <div style="width:45%; padding:10px; box-sizing:border-box; display:flex; flex-direction:column;">
        <div style="height: 90%; padding:10px; box-sizing:border-box;">
          <b style="color:darkred;">Your code</b>
          <textarea id="code" style="color:darkblue; background-color:white;"></textarea>
        </div>
        <div style="height:1%; padding:10px; box-sizing:border-box;">
        </div>
        <div style="height:4%; padding:10px; box-sizing:border-box;">
          <button onclick="run_then_fetch_console()">Run</button>
        </div>
      </div>
      <div style="width:45%; padding:10px; box-sizing:border-box; display:flex; flex-direction:column;">
        <div style="height: 90%; padding:10px; box-sizing:border-box; display:flex; flex-direction:column;">
          <div style="height: 75%; display:flex; flex-direction:column;">
            <b style="color:darkred;">Console</b>
            <textarea id="console" readonly style="background-color:black; color:lime"></textarea>
          </div>
          <div style="height:5%; display:flex; flex-direction:column;">
          </div>
          <div style="height: 20%;">
            <b style="color: darkred;">Input</b>
            <textarea id="input_line" style="background-color:black; color:lime;"></textarea>
          </div>
        </div>
        <div style="height:1%; padding:10px; box-sizing:border-box;">
        </div>
        <div style="height:4%; padding:10px; box-sizing:border-box;">
          <button onclick="feed_input_then_fetch_console()">Enter</button>
        </div>
      </div>
    </div>
    <script>
      async function run_then_fetch_console() {
        code = encodeURI(document.getElementById("code").value).replace(/\+/g, "%2B");

        response = await fetch("/run_then_fetch_console?code="+code)
        j = JSON.parse(await response.json())

        if (typeof j.task_id !== 'undefined')
          globalThis.global_mut_task_id = j.task_id

        if (typeof j.console !== 'undefined')
          render_console(j.console)
      }

      async function feed_input_then_fetch_console() {
        if (globalThis.global_mut_task_id === null)
          return

        input_line = encodeURI(document.getElementById("input_line").value).replace(/\+/g, "%2B");
        document.getElementById("input_line").value = ""

        response = await fetch("/feed_input_then_fetch_console?task_id="+globalThis.global_mut_task_id+"&input_line="+input_line)
        j = JSON.parse(await response.json())

        if (typeof j.console !== 'undefined')
          render_console(j.console)
      }

      async function fetch_console() {
        if (globalThis.global_mut_task_id === null)
          return

        response = await fetch("/fetch_console?task_id="+globalThis.global_mut_task_id)
        j = JSON.parse(await response.json())

        if (typeof j.console !== 'undefined')
          render_console(j.console)
      }

      function render_console(console) {
          document.getElementById("console").value = console
      }

      function clear() {
          document.getElementById("code").value = 'message = "What is your name?"\nprint(message)\nname <- input\nprint("Dear " + name + ", welcome!")\n'
          document.getElementById("console").value = ""
          document.getElementById("input_line").value = ""
      }

      async function main() {
        global_mut_is_running = false
        global_mut_task_id = null

        while (true) {
          await fetch_console()
          await new Promise(r => setTimeout(r, 1000));
        }
      }

      main()
    </script>
  </body>
</html>
"""
}
