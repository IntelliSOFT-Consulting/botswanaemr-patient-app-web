<DataTable rows={rows} headers={headers} {...args}>
3    {({
4      rows,
5      headers,
6      getHeaderProps,
7      getRowProps,
8      getSelectionProps,
9      getToolbarProps,
10      getBatchActionProps,
11      onInputChange,
12      selectedRows,
13      getTableProps,
14      getTableContainerProps,
15    }) => {
16      const batchActionProps = getBatchActionProps();
17
18      return (
 <TableContainer
   title="DataTable"
   description="With batch actions"
   {...getTableContainerProps()}>
   <TableToolbar {...getToolbarProps()}>
     <TableBatchActions {...batchActionProps}>
<TableBatchAction
  tabIndex={batchActionProps.shouldShowBatchActions ? 0 : -1}
  renderIcon={TrashCan}
  onClick={batchActionClick(selectedRows)}>
  Delete
</TableBatchAction>
<TableBatchAction
  tabIndex={batchActionProps.shouldShowBatchActions ? 0 : -1}
  renderIcon={Save}
  onClick={batchActionClick(selectedRows)}>
  Save
</TableBatchAction>
<TableBatchAction
  tabIndex={batchActionProps.shouldShowBatchActions ? 0 : -1}
  renderIcon={Download}
  onClick={batchActionClick(selectedRows)}>
  Download
</TableBatchAction>
     </TableBatchActions>
     <TableToolbarContent
aria-hidden={batchActionProps.shouldShowBatchActions}>
<TableToolbarSearch
  tabIndex={batchActionProps.shouldShowBatchActions ? -1 : 0}
  onChange={onInputChange}
/>
<TableToolbarMenu
  tabIndex={batchActionProps.shouldShowBatchActions ? -1 : 0}>
  <TableToolbarAction onClick={() => alert('Alert 1')}>
    Action 1
  </TableToolbarAction>
  <TableToolbarAction onClick={() => alert('Alert 2')}>
    Action 2
  </TableToolbarAction>
  <TableToolbarAction onClick={() => alert('Alert 3')}>
    Action 3
  </TableToolbarAction>
</TableToolbarMenu>
<Button
  tabIndex={batchActionProps.shouldShowBatchActions ? -1 : 0}
  onClick={action('Add new row')}
  size="small"
  kind="primary">
  Add new
</Button>
     </TableToolbarContent>
   </TableToolbar>
   <Table {...getTableProps()}>
     <TableHead>
<TableRow>
  <TableSelectAll {...getSelectionProps()} />
  {headers.map((header, i) => (
    <TableHeader key={i} {...getHeaderProps({ header })}>
      {header.header}
    </TableHeader>
  ))}
</TableRow>
     </TableHead>
     <TableBody>
{rows.map((row, i) => (
  <TableRow key={i} {...getRowProps({ row })}>
    <TableSelectRow {...getSelectionProps({ row })} />
    {row.cells.map((cell) => (
      <TableCell key={cell.id}>{cell.value}</TableCell>
    ))}
  </TableRow>
))}
     </TableBody>
   </Table>
 </TableContainer>
      );
   }}
  </DataTable>