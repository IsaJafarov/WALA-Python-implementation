# coding=utf-8
# 优先做这个, threading原型
def func2(f3, arg3, a4, a5, a6, a7):
    f3(arg3, a4, a5)
    return arg3


def func3(arg3, a2, a3):
    return arg3


r_args = [func3, "a3", "a4", "a5"]
func2(*r_args, a6="A", a7="B")
# func2(r_args[0], r_args[1], r_args[2], r_args[4], a6="A", a7="B")
