base_init = 10

class T:
    def mythread(self, func, arg):
        func(arg)

def id(x):
    return x

t=T()
t.mythread(arg="A", func=id)

