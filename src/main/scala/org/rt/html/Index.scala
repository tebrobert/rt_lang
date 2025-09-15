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
  <body style="background-color:gray;">
    <div class="container" style="display:flex; height:98%;">
      <div style="width:45%; padding:10px; box-sizing:border-box; display:flex; flex-direction:column;">
        <div style="height: 90%; padding:10px; box-sizing:border-box;">
          <b style="color:darkred;">Your code</b>
          <textarea style="color:darkblue; background-color:white;">message = "What is your name?"&#10;print(message)&#10;name &lt- input&#10;print("Dear " + name + ", welcome!")&#10;</textarea>
        </div>
        <div style="height:1%; padding:10px; box-sizing:border-box;">
        </div>
        <div style="height:4%; padding:10px; box-sizing:border-box;">
          <button>Run</button>
        </div>
      </div>
      <div style="width:45%; padding:10px; box-sizing:border-box; display:flex; flex-direction:column;">
        <div style="height: 90%; padding:10px; box-sizing:border-box; display:flex; flex-direction:column;">
          <div style="height: 75%; display:flex; flex-direction:column;">
            <b style="color:darkred;">Console</b>
            <textarea readonly style="background-color:black; color:lime;">What is your name?&#10;Robert&#10;Dear Robert, welcome!&#10;</textarea>
          </div>
          <div style="height:5%; display:flex; flex-direction:column;">
          </div>
          <div style="height: 20%;">
            <b style="color: darkred;">Input</b>
            <textarea style="background-color:black; color:lime;">Robert</textarea>
          </div>
        </div>
        <div style="height:1%; padding:10px; box-sizing:border-box;">
        </div>
        <div style="height:4%; padding:10px; box-sizing:border-box;">
          <button>Enter</button>
        </div>
      </div>
    </div>
  </body>
</html>
"""
}
