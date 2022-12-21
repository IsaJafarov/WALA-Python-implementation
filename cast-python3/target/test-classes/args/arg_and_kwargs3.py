def thread(func, args):
    return func(*args)

def func2(f3, arg3):
    f3(arg3)
    return arg3

def func3(arg3):
    print(arg3)
    return arg3

thread(func=func2, args=(func3, "a3" ))