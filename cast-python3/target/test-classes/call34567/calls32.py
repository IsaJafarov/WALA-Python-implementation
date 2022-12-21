base_init = 10

def id(x):
    return x

def call(x, y):
    return x(y)

def nothing():
    return 0

z = id(nothing)
z()
