import Blender
from Blender import *
from Blender import Draw, BGL
from Blender.Scene import Render
import math


#	CoR-Blender-Tool										  
#	erstellt eine CoR Einheit aus einem Blender Projekt
#
#	by Michael
#
#	
# - Der Ordner "CoRUnit" mit den Einheitenbildern wird im Blender-StandardRenderverzeichnis erstellt (unter ubuntu/Linux ist das /tmp)
# - Das Objekt das gerendert werden soll muss "Unit" heißen
# - Das Objekt sollte anfangs in Richtung Kamera schauen
	

# config:
path = "//CoRUnit/"

# globale variablen:
idleStartFrame = 1
idleEndFrame = 5

moveStartFrame = 6
moveEndFrame = 10

attackStartFrame = 11
attackEndFrame = 15

dieStartFrame = 16
dieEndFrame = 20


# Funktionen:

# RENDER-Button
def renderButtonClicked(button, val):
	# Los geht's!
	# erst mal Bildtyp, Auflösung etc einstellen:			
	context = Scene.GetCurrent().getRenderingContext() 
	Render.EnableDispWin()
	context.imageType = Render.PNG
	context.imageSizeX(40)
	context.imageSizeY(40)
	# jetzt wird die Einheit gerendert:
	# für jede Animation:
	for animation in "idle","move","attack","die":
		# Animation-Frames einstellen:
		if animation == "idle":
			startFrame = idleStartFrame
			endFrame = idleEndFrame
		if animation == "move":
			startFrame = moveStartFrame
			endFrame = moveEndFrame
		if animation == "attack":
			startFrame = attackStartFrame
			endFrame = attackEndFrame
		if animation == "die":
			startFrame = dieStartFrame 
			endFrame = dieEndFrame
		# für jede Richtung:
		for direction in "N", "NO", "O", "SO","S","SW","W","NW":
			# jeden Frame der Animation rendern:
			for frame in range(startFrame, endFrame):
				# Frame einstellen & rendern:
				context.currentFrame(frame)
				context.render()
				# Pfad für das Bild:
				renderpath = path + "/" + animation + "/" + direction + "/" + format(frame - startFrame)
				# Bild speichern:
				context.saveRenderedImage(renderpath,0)
			# Modell drehen:
			model = Object.Get("Unit")
			oldrot = model.rot.z
			newrot = oldrot + (math.pi / 4)
			if newrot >= 2*math.pi:
				newrot = ((math.pi / 4)-((2*math.pi)-oldrot))
			model.rot = [model.rot.x, model.rot.y, newrot]



# Bei ESCAPE abbrechen:
def event(evt, val):  
	if evt == Draw.ESCKEY:
		Draw.Exit()                
		return
	Draw.Redraw(1)


# Wenn ein Button oder ein Zahlenschieberdingens verändert / angeklickt wird:
def button_event(button, val):
	global idleStartFrame, idleEndFrame, moveStartFrame, moveEndFrame, attackStartFrame, attackEndFrame, dieStartFrame, dieEndFrame
	if button == 1:
		idleStartFrame = val
	if button == 2:
		idleEndFrame = val
	if button == 3:
		moveStartFrame = val
	if button == 4:
		moveEndFrame = val
	if button == 5:
		attackStartFrame = val
	if button == 6:
		attackEndFrame = val
	if button == 7:
		dieStartFrame = val
	if button == 8:
		dieEndFrame = val

		

		

# zeichnet die GUI:
def gui():
  	BGL.glClearColor(1,1,1,1)
  	BGL.glClear(BGL.GL_COLOR_BUFFER_BIT)

	# idle
	Draw.Label("Idle-Animation:", 10,200,180,20)
  	Draw.Number("von ", 1, 10,180,90,20,idleStartFrame,0,1000, "", button_event, 1,1)
  	Draw.Number("bis ", 2, 110,180,90,20,idleEndFrame,0,1000, "", button_event, 1,1)

	# move
	Draw.Label("Move-Animation:", 10,150,180,20)
  	Draw.Number("von ", 3, 10,130,90,20,moveStartFrame,0,1000, "", button_event, 1,1)
  	Draw.Number("bis ", 4, 110,130,90,20,moveEndFrame,0,1000, "", button_event, 1,1)

	# attack
	Draw.Label("Attack-Animation:", 10,90,180,20)
  	Draw.Number("von ", 5, 10,70,90,20,attackStartFrame,0,1000, "", button_event, 1,1)
  	Draw.Number("bis ", 6, 110,70,90,20,attackEndFrame,0,1000, "", button_event, 1,1)

	# die
	Draw.Label("Die-Animation:", 10,40,180,20)
  	Draw.Number("von ", 7, 10,20,90,20,dieStartFrame,0,1000, "", button_event, 1,1)
  	Draw.Number("bis ", 8, 110,20,90,20,dieEndFrame,0,1000, "", button_event, 1,1)

	# RENDER-Button:
	Draw.Button("RENDER", 9, 230,30,80,20, "RENDER!", renderButtonClicked)




# callbacks registrieren:
Draw.Register(gui, event)

