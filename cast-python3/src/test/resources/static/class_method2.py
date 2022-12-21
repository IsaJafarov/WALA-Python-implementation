class A:

    @classmethod
    def class_func(cls, func3, arg4):
        # cls.static_func1(func3, arg4)
        return func3(arg4)


def func(arg):
    print("func", arg)


a = A()
a.class_func(func, "a")
