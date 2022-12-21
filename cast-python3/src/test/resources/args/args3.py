def func1(func, args):
    return func(*args)

def func2(f3, arg3):
    f3(arg3)
    return arg3

def func3(arg3):
    print(arg3)
    return arg3

r_args=[func3, "a3" ]
func1(func2, r_args)