def func1(*args, **kwargs):
    return args[0](kwargs['a'])

def func2(arg):
    print(arg)
    return arg


r_args=[func2]
r_kwargs={"a":"AAA"}
func1(*r_args, **r_kwargs)