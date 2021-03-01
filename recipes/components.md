# Component Specification v1

```json
"components": [
    "{key}": { 
        /* component definition */
    },

    ...
]
```


## Component Definitions

### Match Block with Properties
```json
{
    "type": "compactcrafting:match_block",
    "block": "minecraft:block_id_here",
    "properties": {
        "{property}": "{value}"
    }
}
```

### Match Itemstack Entity (Future)
```json
{
    "type": "compactcrafting:match_itemstack"
}
```

### Match Specific Entity (Future)
```json
{
    "type": "compactcrafting:match_entity"
}
```