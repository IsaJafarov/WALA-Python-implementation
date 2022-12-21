# 所有做完的效果
def func1(func1, func2, arg1, func3):
    func2(func3, arg1)


def func5(func, arg):
    func(arg)
    return arg


def func6(arg):
    print("func6")
    return arg


def func7(arg):
    print("func7")
    return arg


args = [func5, "AAA"]
kwargs = {"func3": func6}

func1(func7, *args, **kwargs)
