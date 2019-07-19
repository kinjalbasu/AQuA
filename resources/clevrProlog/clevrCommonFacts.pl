similar(X,Y) :- is_similar(X,Y).
similar(X,Y) :- is_similar(Y,X).

is_property(red,color).
is_property(green,color).
is_property(purple,color).
is_property(pink,color).
is_property(yellow,color).
is_property(blue,color).
is_property(cyan,color).
is_property(gray,color).

is_property(cube,shape).
is_property(cylinder,shape).
is_property(sphere,shape).

is_property(nonmetal,material).
is_property(metal,material).
is_property(matte,material).
is_property(rubber,material).

is_property(small,size).
is_property(medium,size).
is_property(large,size).
is_property(big,size).
is_property(tiny,size).

is_similar(block,cube).
is_similar(ball,sphere).
is_similar(metallic,metal).
is_similar(shiny,metal).


