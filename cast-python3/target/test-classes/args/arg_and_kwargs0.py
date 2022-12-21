def func1(func, arg):
    return func(arg)

def func2(func, arg):
    return func(arg)

def func3(arg):
    return arg

func1(func3,1,func=1, arg="A")
func2(1,1,func=func3, arg="A")
