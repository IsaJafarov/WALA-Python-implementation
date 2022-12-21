base_init = 10

def id(x):
    return x

def call(x, y):
    return x(y)

def foo(a,b):
    return call(id, a+b)

print(foo(1,2))

