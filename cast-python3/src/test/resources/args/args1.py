def func1(*args):
    return args[0](args[1])

def func2(arg):
    print(arg)
    return arg

func1(func2, "A")