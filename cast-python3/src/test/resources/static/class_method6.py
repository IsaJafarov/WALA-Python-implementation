class A:

    def __init__(self, func):
        self.func = func

    @classmethod
    def class_func2(cls, func3, arg4):
        return func3(arg4)

def func(arg):
    print("func", arg)


a = A(func)
a.class_func2(func, "a")
