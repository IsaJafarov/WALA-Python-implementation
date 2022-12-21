base_init = 10

def id(x):
    return x

def call(x, y):
    return x(y)

def foo(a,b):
    return call(id, a+b)

class Foo(object):
    base = base_init
    
    def foo(self, a, b):
        self.contents = id(a+b+self.base)
        return self.contents

print(foo(1,2))

instance = Foo()
print(Foo.foo(instance, 2,3))
print(instance.foo(2,3))
