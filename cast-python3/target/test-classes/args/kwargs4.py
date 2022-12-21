def func2(f3, arg3, f4, arg5):
    f3(arg3, f4, arg5)
    return arg3


def func3(arg3, f4, arg5):
    f4(arg5)
    return arg3


def func4(arg3):
    return arg3


kw_args = {"f4": func4, "arg5": "a3"}
func2(func3, arg3="a3", **kw_args)

