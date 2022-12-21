def func1(**kwargs):
    return kwargs['f'](kwargs['a'])

def func2(arg):
    print(arg)
    return arg

func1(f=func2, a="AAA")