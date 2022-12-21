class A:
    @staticmethod
    def static_func1(func2, arg3):
        return func2(arg3)

    def func3(self, arg3):
        return self.func(arg3)


def func(arg):
    print("func", arg)


a = A()
a.static_func1(func, "a")  # no
