class A:
    def __init__(self, func):
        self.func = func

    @staticmethod
    def static_func1(func2, arg3):
        return func2(arg3)

    def func3(self, arg3):
        return self.func(arg3)


def func(arg):
    print("func", arg)



A.static_func1(func, "a") # yes

# a = A(func)
# a.static_func1(func, "a") # no
# a.class_func2(func, "a") # yes
# a.func3("a") # yes
