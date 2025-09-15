package org.rt.html

object Index {
  val page = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Three Text Areas Layout with Buttons</title>
  <style>
    html, body {
      height: 100%;
      margin: 0;
    }

    .container {
      display: flex;
      height: 98vh;
    }

    .left {
      width: 45%;
      padding: 10px;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
    }

    .right {
      width: 45%;
      display: flex;
      flex-direction: column;
    }

    .top-right {
      height: 64vh;
      padding: 10px;
      box-sizing: border-box;
    }

    .middle-right {
      height: 5vh;
      padding: 10px;
      box-sizing: border-box;
    }

    .bottom-right {
      height: 20vh;
      padding: 10px;
      box-sizing: border-box;
    }

    textarea {
      width: 100%;
      height: 100%;
      resize: none;
      font-size: 20pt;
      font-family: 'Courier New', Courier, monospace;
    }

    button {
      margin-top: 10px;
      padding: 10px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="left">
      <label for="left-textarea">Your code</label>
      <textarea>
message = "What is your name?"
print(message)
name &lt- input
print("Dear " + name + ", welcome!")
</textarea>
      <button>Run</button>
    </div>
    <div class="right">
      <div class="top-right">
        <label for="left-textarea">Console</label>
        <textarea readonly></textarea>
      </div>
      <div class="middle-right">
      </div>
      <div class="bottom-right">
        <label for="left-textarea">Input</label>
        <textarea></textarea>
        <button>Enter</button>
      </div>
    </div>
  </div>
</body>
</html>
"""
}
