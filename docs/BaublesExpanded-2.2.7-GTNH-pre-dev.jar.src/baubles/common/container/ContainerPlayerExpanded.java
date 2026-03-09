package baubles.common.container;

import baubles.api.expanded.BaubleExpandedSlots;
import baubles.common.BaublesConfig;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;




public class ContainerPlayerExpanded
  extends Container
{
  public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
  public IInventory craftResult = (IInventory)new InventoryCraftResult();
  
  public InventoryBaubles baubles;
  
  private final EntityPlayer thePlayer;
  private int slotsAdded = 0;
  
  private int baubleFirstSlotIndex = -1;
  private int baubleSlotCount = 0;
  
  public ContainerPlayerExpanded(InventoryPlayer playerInv, boolean isClient, EntityPlayer player) {
    this.thePlayer = player;
    this.baubles = PlayerHandler.getPlayerBaubles(player);
    this.baubles.setEventHandler(this);




    
    if (!BaublesConfig.useOldGuiRendering) {
      addSlotToContainer((Slot)new SlotCrafting(playerInv.player, (IInventory)this.craftMatrix, this.craftResult, 0, 144, 36));
      
      for (int j = 0; j < 2; j++) {
        for (final int k = 0; k < 2; k++) {
          addSlotToContainer(new Slot((IInventory)this.craftMatrix, k + j * 2, 88 + k * 18, 26 + j * 18));
        }
      } 
    } 
    
    int i;
    for (i = 0; i < 4; i++) {
      final int k = i;
      addSlotToContainer(new Slot((IInventory)playerInv, playerInv.getSizeInventory() - 1 - i, 8, 8 + i * 18) {
            public int getSlotStackLimit() {
              return 1;
            }
            
            public boolean isItemValid(ItemStack itemStack) {
              if (itemStack == null || itemStack.getItem() == null) return false; 
              return itemStack.getItem().isValidArmor(itemStack, k, (Entity)ContainerPlayerExpanded.this.thePlayer);
            }

            
            @SideOnly(Side.CLIENT)
            public IIcon getBackgroundIconIndex() {
              return ItemArmor.func_94602_b(k);
            }
          });
    } 
    
    int slotOffset = 18;
    int slotStartX = 80;
    int slotStartY = 8;

    
    this.baubleFirstSlotIndex = this.slotsAdded;
    for (i = 0; i < 20; i++) {
      String slotType = BaubleExpandedSlots.getSlotType(i);
      if (BaublesConfig.showUnusedSlots || !slotType.equals("unknown")) {

        
        Slot slot = BaublesConfig.useOldGuiRendering ? new SlotBauble(this.baubles, slotType, i, 80 + 18 * i / 4, 8 + 18 * i % 4) : new SlotBauble(this.baubles, slotType, i, -18, 12 + 18 * i);
        
        addSlotToContainer(slot);
        this.baubleSlotCount++;
      } 
    } 

    
    for (i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        addSlotToContainer(new Slot((IInventory)playerInv, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
      }
    } 

    
    for (i = 0; i < 9; i++) {
      addSlotToContainer(new Slot((IInventory)playerInv, i, 8 + i * 18, 142));
    }
    
    if (!BaublesConfig.useOldGuiRendering) {
      onCraftMatrixChanged((IInventory)this.craftMatrix);
      scrollTo(0.0F);
    } 
  }


  
  protected Slot addSlotToContainer(Slot slot) {
    this.slotsAdded++;
    return super.addSlotToContainer(slot);
  }

  
  public void onCraftMatrixChanged(IInventory par1IInventory) {
    if (!BaublesConfig.useOldGuiRendering) {
      this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.thePlayer.worldObj));
    }
  }

  
  public void onContainerClosed(EntityPlayer player) {
    super.onContainerClosed(player);
    if (BaublesConfig.useOldGuiRendering) {
      return;
    }
    for (int i = 0; i < 4; i++) {
      ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
      
      if (itemstack != null && 
        !player.inventory.addItemStackToInventory(itemstack)) {
        player.dropPlayerItemWithRandomChoice(itemstack, false);
      }
    } 

    
    this.craftResult.setInventorySlotContents(0, null);
    if (!player.worldObj.isRemote) {
      PlayerHandler.setPlayerBaubles(player, this.baubles);
    }
  }
  
  public int getBaubleSlotCount() {
    return this.baubleSlotCount;
  }
  
  public SlotBauble getBaubleSlot(int slotIndex) {
    if (slotIndex < 0 || slotIndex >= this.baubleSlotCount) return null; 
    return this.inventorySlots.get(this.baubleFirstSlotIndex + slotIndex);
  }
  
  public void scrollTo(float offset) {
    if (!canScroll())
      return;  int activeBaubleSlots = BaubleExpandedSlots.slotsCurrentlyUsed();
    
    offset = Math.max(0.0F, Math.min(1.0F, offset));
    
    int shownSlots = 8;
    int slotOffset = (int)(offset * (activeBaubleSlots - shownSlots) + 0.5F);
    
    if (slotOffset < 0) {
      slotOffset = 0;
    }
    
    for (int i = 0; i < activeBaubleSlots && i < 20; i++) {
      Slot slot = this.inventorySlots.get(this.baubleFirstSlotIndex + i);
      if (i >= 0) {
        slot.yDisplayPosition = 12 - slotOffset * 18 + i * 18;
        if (slot.yDisplayPosition < 12 || slot.yDisplayPosition > 144)
        {
          slot.yDisplayPosition = -2000;
        }
      } 
    } 
  }
  
  public boolean canScroll() {
    return (BaubleExpandedSlots.slotsCurrentlyUsed() > 8 && !BaublesConfig.useOldGuiRendering);
  }

  
  public boolean canInteractWith(EntityPlayer player) {
    return true;
  }





























  
  public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
    // Byte code:
    //   0: aconst_null
    //   1: astore_3
    //   2: aload_0
    //   3: getfield inventorySlots : Ljava/util/List;
    //   6: iload_2
    //   7: invokeinterface get : (I)Ljava/lang/Object;
    //   12: checkcast net/minecraft/inventory/Slot
    //   15: astore #4
    //   17: invokestatic slotsCurrentlyUsed : ()I
    //   20: istore #5
    //   22: iconst_5
    //   23: istore #6
    //   25: getstatic baubles/common/BaublesConfig.useOldGuiRendering : Z
    //   28: ifeq -> 34
    //   31: iconst_0
    //   32: istore #6
    //   34: aload #4
    //   36: ifnull -> 47
    //   39: aload #4
    //   41: invokevirtual getHasStack : ()Z
    //   44: ifne -> 49
    //   47: aload_3
    //   48: areturn
    //   49: aload #4
    //   51: invokevirtual getStack : ()Lnet/minecraft/item/ItemStack;
    //   54: astore #7
    //   56: aload #7
    //   58: invokevirtual copy : ()Lnet/minecraft/item/ItemStack;
    //   61: astore_3
    //   62: aload_3
    //   63: invokevirtual getItem : ()Lnet/minecraft/item/Item;
    //   66: astore #8
    //   68: getstatic baubles/common/BaublesConfig.useOldGuiRendering : Z
    //   71: ifne -> 148
    //   74: iload_2
    //   75: ifne -> 116
    //   78: aload_0
    //   79: aload #7
    //   81: iconst_4
    //   82: iload #6
    //   84: iadd
    //   85: iload #5
    //   87: iadd
    //   88: bipush #40
    //   90: iload #6
    //   92: iadd
    //   93: iload #5
    //   95: iadd
    //   96: iconst_1
    //   97: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZ)Z
    //   100: ifne -> 105
    //   103: aconst_null
    //   104: areturn
    //   105: aload #4
    //   107: aload #7
    //   109: aload_3
    //   110: invokevirtual onSlotChange : (Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V
    //   113: goto -> 577
    //   116: iload_2
    //   117: iconst_5
    //   118: if_icmpge -> 577
    //   121: aload_0
    //   122: aload #7
    //   124: iconst_4
    //   125: iload #6
    //   127: iadd
    //   128: iload #5
    //   130: iadd
    //   131: bipush #40
    //   133: iload #6
    //   135: iadd
    //   136: iload #5
    //   138: iadd
    //   139: iconst_0
    //   140: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZ)Z
    //   143: ifne -> 577
    //   146: aconst_null
    //   147: areturn
    //   148: aload #8
    //   150: instanceof net/minecraft/item/ItemArmor
    //   153: ifeq -> 220
    //   156: aload #8
    //   158: checkcast net/minecraft/item/ItemArmor
    //   161: astore #9
    //   163: aload_0
    //   164: getfield inventorySlots : Ljava/util/List;
    //   167: iload #6
    //   169: aload #9
    //   171: getfield armorType : I
    //   174: iadd
    //   175: invokeinterface get : (I)Ljava/lang/Object;
    //   180: checkcast net/minecraft/inventory/Slot
    //   183: invokevirtual getHasStack : ()Z
    //   186: ifne -> 220
    //   189: iload #6
    //   191: aload #9
    //   193: getfield armorType : I
    //   196: iadd
    //   197: istore #11
    //   199: aload_0
    //   200: aload #7
    //   202: iload #11
    //   204: iload #11
    //   206: iconst_1
    //   207: iadd
    //   208: iconst_0
    //   209: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZ)Z
    //   212: ifne -> 217
    //   215: aconst_null
    //   216: astore_3
    //   217: goto -> 577
    //   220: iload_2
    //   221: iconst_4
    //   222: iload #6
    //   224: iadd
    //   225: iload #5
    //   227: iadd
    //   228: if_icmplt -> 440
    //   231: aload #8
    //   233: instanceof baubles/api/IBauble
    //   236: ifeq -> 440
    //   239: aload #8
    //   241: checkcast baubles/api/IBauble
    //   244: astore #10
    //   246: aload #10
    //   248: aload_3
    //   249: aload_0
    //   250: getfield thePlayer : Lnet/minecraft/entity/player/EntityPlayer;
    //   253: invokeinterface canEquip : (Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;)Z
    //   258: ifeq -> 440
    //   261: iconst_4
    //   262: iload #6
    //   264: iadd
    //   265: istore #11
    //   267: iload #11
    //   269: iconst_4
    //   270: iload #6
    //   272: iadd
    //   273: iload #5
    //   275: iadd
    //   276: if_icmpge -> 437
    //   279: aload_3
    //   280: ifnonnull -> 286
    //   283: goto -> 437
    //   286: aload_0
    //   287: getfield inventorySlots : Ljava/util/List;
    //   290: iload #11
    //   292: invokeinterface get : (I)Ljava/lang/Object;
    //   297: checkcast net/minecraft/inventory/Slot
    //   300: invokevirtual getHasStack : ()Z
    //   303: ifeq -> 309
    //   306: goto -> 431
    //   309: aload #8
    //   311: instanceof baubles/api/expanded/IBaubleExpanded
    //   314: ifeq -> 333
    //   317: aload #8
    //   319: checkcast baubles/api/expanded/IBaubleExpanded
    //   322: aload_3
    //   323: invokeinterface getBaubleTypes : (Lnet/minecraft/item/ItemStack;)[Ljava/lang/String;
    //   328: astore #12
    //   330: goto -> 353
    //   333: iconst_1
    //   334: anewarray java/lang/String
    //   337: dup
    //   338: iconst_0
    //   339: aload #10
    //   341: aload_3
    //   342: invokeinterface getBaubleType : (Lnet/minecraft/item/ItemStack;)Lbaubles/api/BaubleType;
    //   347: invokestatic getTypeFromBaubleType : (Lbaubles/api/BaubleType;)Ljava/lang/String;
    //   350: aastore
    //   351: astore #12
    //   353: aload #12
    //   355: astore #13
    //   357: aload #13
    //   359: arraylength
    //   360: istore #14
    //   362: iconst_0
    //   363: istore #15
    //   365: iload #15
    //   367: iload #14
    //   369: if_icmpge -> 431
    //   372: aload #13
    //   374: iload #15
    //   376: aaload
    //   377: astore #16
    //   379: aload #16
    //   381: ldc 'universal'
    //   383: invokevirtual equals : (Ljava/lang/Object;)Z
    //   386: ifne -> 407
    //   389: aload #16
    //   391: iload #11
    //   393: iconst_4
    //   394: isub
    //   395: iload #6
    //   397: isub
    //   398: invokestatic getSlotType : (I)Ljava/lang/String;
    //   401: invokevirtual equals : (Ljava/lang/Object;)Z
    //   404: ifeq -> 425
    //   407: aload_0
    //   408: aload #7
    //   410: iload #11
    //   412: iload #11
    //   414: iconst_1
    //   415: iadd
    //   416: iconst_0
    //   417: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZ)Z
    //   420: ifne -> 425
    //   423: aconst_null
    //   424: astore_3
    //   425: iinc #15, 1
    //   428: goto -> 365
    //   431: iinc #11, 1
    //   434: goto -> 267
    //   437: goto -> 577
    //   440: iload_2
    //   441: iconst_4
    //   442: iload #6
    //   444: iadd
    //   445: iload #5
    //   447: iadd
    //   448: if_icmplt -> 494
    //   451: iload_2
    //   452: bipush #31
    //   454: iload #6
    //   456: iadd
    //   457: iload #5
    //   459: iadd
    //   460: if_icmpge -> 494
    //   463: aload_0
    //   464: aload #7
    //   466: bipush #31
    //   468: iload #6
    //   470: iadd
    //   471: iload #5
    //   473: iadd
    //   474: bipush #40
    //   476: iload #6
    //   478: iadd
    //   479: iload #5
    //   481: iadd
    //   482: iconst_0
    //   483: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZ)Z
    //   486: ifne -> 577
    //   489: aconst_null
    //   490: astore_3
    //   491: goto -> 577
    //   494: iload_2
    //   495: bipush #31
    //   497: iload #6
    //   499: iadd
    //   500: iload #5
    //   502: iadd
    //   503: if_icmplt -> 548
    //   506: iload_2
    //   507: bipush #40
    //   509: iload #6
    //   511: iadd
    //   512: iload #5
    //   514: iadd
    //   515: if_icmpge -> 548
    //   518: aload_0
    //   519: aload #7
    //   521: iconst_4
    //   522: iload #6
    //   524: iadd
    //   525: iload #5
    //   527: iadd
    //   528: bipush #31
    //   530: iload #6
    //   532: iadd
    //   533: iload #5
    //   535: iadd
    //   536: iconst_0
    //   537: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZ)Z
    //   540: ifne -> 577
    //   543: aconst_null
    //   544: astore_3
    //   545: goto -> 577
    //   548: aload_0
    //   549: aload #7
    //   551: iconst_4
    //   552: iload #6
    //   554: iadd
    //   555: iload #5
    //   557: iadd
    //   558: bipush #40
    //   560: iload #6
    //   562: iadd
    //   563: iload #5
    //   565: iadd
    //   566: iconst_0
    //   567: aload #4
    //   569: invokevirtual mergeItemStack : (Lnet/minecraft/item/ItemStack;IIZLnet/minecraft/inventory/Slot;)Z
    //   572: ifne -> 577
    //   575: aconst_null
    //   576: astore_3
    //   577: aload #7
    //   579: getfield stackSize : I
    //   582: ifgt -> 594
    //   585: aload #4
    //   587: aconst_null
    //   588: invokevirtual putStack : (Lnet/minecraft/item/ItemStack;)V
    //   591: goto -> 599
    //   594: aload #4
    //   596: invokevirtual onSlotChanged : ()V
    //   599: aload_3
    //   600: ifnull -> 617
    //   603: aload #7
    //   605: getfield stackSize : I
    //   608: aload_3
    //   609: getfield stackSize : I
    //   612: if_icmpne -> 617
    //   615: aconst_null
    //   616: astore_3
    //   617: aload #4
    //   619: aload_1
    //   620: aload #7
    //   622: invokevirtual onPickupFromSlot : (Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V
    //   625: aload_3
    //   626: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #199	-> 0
    //   #200	-> 2
    //   #201	-> 17
    //   #202	-> 22
    //   #203	-> 25
    //   #204	-> 31
    //   #207	-> 34
    //   #208	-> 47
    //   #210	-> 49
    //   #211	-> 56
    //   #212	-> 62
    //   #214	-> 68
    //   #215	-> 74
    //   #216	-> 78
    //   #217	-> 103
    //   #219	-> 105
    //   #220	-> 116
    //   #221	-> 121
    //   #222	-> 146
    //   #225	-> 148
    //   #226	-> 189
    //   #227	-> 199
    //   #228	-> 215
    //   #230	-> 217
    //   #231	-> 261
    //   #232	-> 279
    //   #233	-> 283
    //   #235	-> 286
    //   #236	-> 306
    //   #239	-> 309
    //   #240	-> 317
    //   #242	-> 333
    //   #244	-> 353
    //   #245	-> 379
    //   #246	-> 398
    //   #247	-> 417
    //   #248	-> 423
    //   #244	-> 425
    //   #231	-> 431
    //   #252	-> 440
    //   #253	-> 463
    //   #254	-> 489
    //   #256	-> 494
    //   #257	-> 518
    //   #258	-> 543
    //   #260	-> 548
    //   #261	-> 575
    //   #264	-> 577
    //   #265	-> 585
    //   #267	-> 594
    //   #270	-> 599
    //   #271	-> 615
    //   #274	-> 617
    //   #276	-> 625
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   199	18	11	armorSlot	I
    //   163	57	9	armor	Lnet/minecraft/item/ItemArmor;
    //   330	3	12	types	[Ljava/lang/String;
    //   379	46	16	type	Ljava/lang/String;
    //   353	78	12	types	[Ljava/lang/String;
    //   267	170	11	baubleSlot	I
    //   246	194	10	bauble	Lbaubles/api/IBauble;
    //   0	627	0	this	Lbaubles/common/container/ContainerPlayerExpanded;
    //   0	627	1	player	Lnet/minecraft/entity/player/EntityPlayer;
    //   0	627	2	slotIndex	I
    //   2	625	3	returnStack	Lnet/minecraft/item/ItemStack;
    //   17	610	4	slot	Lnet/minecraft/inventory/Slot;
    //   22	605	5	visibleBaubleSlots	I
    //   25	602	6	craftingActive	I
    //   56	571	7	originalStack	Lnet/minecraft/item/ItemStack;
    //   68	559	8	item	Lnet/minecraft/item/Item;
  }





























  
  private void unequipBauble(ItemStack stack) {}




























  
  public void putStacksInSlots(ItemStack[] p_75131_1_) {
    this.baubles.blockEvents = true;
    super.putStacksInSlots(p_75131_1_);
  }
  
  protected boolean mergeItemStack(ItemStack sourceStack, int startIndex, int endIndex, boolean reverse, Slot sourceSlot) {
    boolean merged = false;
    int index = reverse ? (endIndex - 1) : startIndex;




    
    if (sourceStack.isStackable()) {
      while (sourceStack.stackSize > 0 && ((!reverse && index < endIndex) || (reverse && index >= startIndex))) {
        Slot targetSlot = this.inventorySlots.get(index);
        ItemStack targetStack = targetSlot.getStack();
        
        if (targetStack != null && targetStack.getItem() == sourceStack.getItem() && (
          !sourceStack.getHasSubtypes() || sourceStack.getItemDamage() == targetStack.getItemDamage()) && 
          ItemStack.areItemStackTagsEqual(sourceStack, targetStack)) {
          
          int combinedSize = targetStack.stackSize + sourceStack.stackSize;
          
          if (combinedSize <= sourceStack.getMaxStackSize()) {
            if (sourceSlot instanceof SlotBauble) {
              unequipBauble(sourceStack);
            }
            sourceStack.stackSize = 0;
            targetStack.stackSize = combinedSize;
            targetSlot.onSlotChanged();
            return true;
          }  if (targetStack.stackSize < sourceStack.getMaxStackSize()) {
            if (sourceSlot instanceof SlotBauble) {
              unequipBauble(sourceStack);
            }
            sourceStack.stackSize -= sourceStack.getMaxStackSize() - targetStack.stackSize;
            targetStack.stackSize = sourceStack.getMaxStackSize();
            targetSlot.onSlotChanged();
            merged = true;
          } 
        } 
        
        index = reverse ? (index - 1) : (index + 1);
      } 
    }

    
    index = reverse ? (endIndex - 1) : startIndex;
    while ((!reverse && index < endIndex) || (reverse && index >= startIndex)) {
      Slot targetSlot = this.inventorySlots.get(index);
      ItemStack targetStack = targetSlot.getStack();
      
      if (targetStack == null) {
        if (sourceSlot instanceof SlotBauble) {
          unequipBauble(sourceStack);
        }
        targetSlot.putStack(sourceStack.copy());
        targetSlot.onSlotChanged();
        sourceStack.stackSize = 0;
        return true;
      } 
      
      index = reverse ? (index - 1) : (index + 1);
    } 
    
    return merged;
  }
}
