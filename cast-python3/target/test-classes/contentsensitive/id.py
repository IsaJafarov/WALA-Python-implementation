def t(f):
    f()

def f1():
    pass

def f2():
    pass

if __name__ == '__main__':
    t(f1)
    t(f2)