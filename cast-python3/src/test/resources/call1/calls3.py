base_init = 10


def id(x):
    return x


def call(x, y):
    return x(y)


def foo(a, b):
    return call(id, a + b)


class Foo(object):
    base = base_init

    def foo(self, a, b):
        self.contents = id(a + b + self.base)
        return self.contents


instance = Foo()
instance.f = foo
print(instance.f(4, 5))
print(instance.f)
#
# instance.foo = foo;
# print(instance.foo(5,6))
# print(instance.foo);
#
# foo.x = foo;
# print(foo.x(6,7));
# print(foo.x);
#
# x = Foo
# print(x)
# y = x()
# print(y)
# print(y.foo(7,8))
#
# def nothing():
#     return 0
#
# z = id(nothing)
# z()
