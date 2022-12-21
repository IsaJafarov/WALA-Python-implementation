def func1(func, *args, **kwargs):
    return args[0](kwargs['a'])

def func2(arg):
    return arg

def func3(arg):
    return arg

func1(func2, func3, a="AAA")