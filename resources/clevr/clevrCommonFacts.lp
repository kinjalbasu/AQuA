similar(X,Y) :- is_similar(X,Y).
similar(X,Y) :- is_similar(Y,X).

is_property(red,color).
is_property(green,color).
is_property(purple,color).
is_property(pink, color).
is_property(yellow, color).
is_property(blue,color).

is_property(cube,shape).
is_property(cylinder,shape).
is_property(sphere,shape).

is_property(nonmetal,material).
is_property(metal,material).

is_similar(block,cube).
is_similar(ball,sphere).
