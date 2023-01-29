def TRY(action):
    try:
        return action()
    except Exception as e:
        return e