def func1(*args, **kwargs):
    return args[0](kwargs['a'])

def func2(arg):
    return arg

func1(func2, a="AAA")