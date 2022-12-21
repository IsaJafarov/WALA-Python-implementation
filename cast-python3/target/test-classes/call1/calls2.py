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

instance = Foo()
f = instance.foo
print(f)
print(f(3,4))
