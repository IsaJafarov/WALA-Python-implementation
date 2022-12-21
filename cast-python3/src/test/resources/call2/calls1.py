base_init = 10

def mythread(func, arg):
    func(arg)

def id(x):
    return x

mythread(arg="A", func=id)

