def rt_try(action):
    try:
        return action()
    except Exception as e:
        return e
