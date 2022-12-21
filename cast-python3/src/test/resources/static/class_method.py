class A:

    @classmethod
    def class_func(cls, func3, arg4):
        return func3(arg4)


def func(arg):
    print("func", arg)


A.class_func(func, "a")  # yes
# A.class_func2(func, "a") # no func3 in ssa is v3 but func is v2
# A.B.C.func4()
#
# a = A(func)
# a.static_func1(func, "a") # no
# a.class_func2(func, "a") # yes
# a.func3("a") # yes
